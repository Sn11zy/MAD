package com.example.sportsorganizer.utils

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PasswordHashing {
    companion object {
        // Stored formats supported:
        // 1) algorithm:iterations:saltBase64:hashBase64  (preferred)
        // 2) iterations:saltBase64:hashBase64           (legacy - assumes PBKDF2WithHmacSHA256)
        private const val PREFERRED_ALGO = "PBKDF2WithHmacSHA256"
        private const val FALLBACK_ALGO = "PBKDF2WithHmacSHA1"

        private const val ITERATIONS = 100_000
        private const val SALT_LENGTH = 16 // bytes
        private const val KEY_LENGTH = 256 // bits

        private fun selectAvailableAlgorithm(): String =
            try {
                SecretKeyFactory.getInstance(PREFERRED_ALGO)
                PREFERRED_ALGO
            } catch (_: Throwable) {
                // prefer the fallback if preferred not available
                try {
                    SecretKeyFactory.getInstance(FALLBACK_ALGO)
                    FALLBACK_ALGO
                } catch (_: Throwable) {
                    // if neither is available, rethrow the original exception by attempting preferred again
                    PREFERRED_ALGO
                }
            }

        fun hashPassword(password: String): String {
            val random = SecureRandom()
            val salt = ByteArray(SALT_LENGTH).also { random.nextBytes(it) }

            val algorithm = selectAvailableAlgorithm()
            val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val skf = SecretKeyFactory.getInstance(algorithm)
            val hash = skf.generateSecret(spec).encoded

            val encoder = Base64.getEncoder()
            // store algorithm to ensure verify uses same KDF
            return "$algorithm:$ITERATIONS:${encoder.encodeToString(salt)}:${encoder.encodeToString(hash)}"
        }

        fun verifyPassword(
            inputPassword: String,
            storedHash: String,
        ): Boolean {
            try {
                val parts = storedHash.split(":")
                val algorithm: String
                val iterations: Int
                val salt: ByteArray
                val stored: ByteArray

                val decoder = Base64.getDecoder()

                if (parts.size == 4) {
                    algorithm = parts[0]
                    iterations = parts[1].toIntOrNull() ?: return false
                    salt =
                        try {
                            decoder.decode(parts[2])
                        } catch (_: IllegalArgumentException) {
                            return false
                        }
                    stored =
                        try {
                            decoder.decode(parts[3])
                        } catch (_: IllegalArgumentException) {
                            return false
                        }
                } else if (parts.size == 3) {
                    // legacy format: assume preferred algorithm
                    algorithm = PREFERRED_ALGO
                    iterations = parts[0].toIntOrNull() ?: return false
                    salt =
                        try {
                            decoder.decode(parts[1])
                        } catch (_: IllegalArgumentException) {
                            return false
                        }
                    stored =
                        try {
                            decoder.decode(parts[2])
                        } catch (_: IllegalArgumentException) {
                            return false
                        }
                } else {
                    return false
                }

                // Try to obtain a SecretKeyFactory for the stored algorithm; if unavailable,
                // fall back to preferred and then fallback algorithm to maximize compatibility.
                val skf =
                    try {
                        SecretKeyFactory.getInstance(algorithm)
                    } catch (_: Throwable) {
                        try {
                            SecretKeyFactory.getInstance(PREFERRED_ALGO)
                        } catch (_: Throwable) {
                            try {
                                SecretKeyFactory.getInstance(FALLBACK_ALGO)
                            } catch (_: Throwable) {
                                return false
                            }
                        }
                    }

                val spec = PBEKeySpec(inputPassword.toCharArray(), salt, iterations, stored.size * 8)
                val testHash = skf.generateSecret(spec).encoded

                return constantTimeArrayEquals(stored, testHash)
            } catch (_: Exception) {
                // For any parsing/crypto errors, do not throw; return false to indicate verification failed.
                return false
            }
        }

        private fun constantTimeArrayEquals(
            a: ByteArray,
            b: ByteArray,
        ): Boolean {
            if (a.size != b.size) return false
            var result = 0
            for (i in a.indices) {
                result = result or (a[i].toInt() xor b[i].toInt())
            }
            return result == 0
        }
    }
}
