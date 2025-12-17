package com.example.sportsorganizer.utils

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Utility class for secure password hashing and verification using PBKDF2.
 *
 * This class provides methods to hash passwords with salt and verify passwords against
 * stored hashes. It uses PBKDF2 (Password-Based Key Derivation Function 2) with either
 * HMAC-SHA256 (preferred) or HMAC-SHA1 (fallback) algorithms for maximum compatibility
 * across different Android API levels.
 *
 * The implementation includes:
 * - Automatic algorithm selection based on availability
 * - Secure random salt generation
 * - Constant-time comparison to prevent timing attacks
 * - Support for legacy hash format migration
 *
 * @see hashPassword
 * @see verifyPassword
 */
class PasswordHashing {
    companion object {
        /** Preferred PBKDF2 algorithm using HMAC-SHA256 */
        private const val PREFERRED_ALGO = "PBKDF2WithHmacSHA256"

        /** Fallback PBKDF2 algorithm using HMAC-SHA1 for older Android versions */
        private const val FALLBACK_ALGO = "PBKDF2WithHmacSHA1"

        /** Number of iterations for PBKDF2 key derivation */
        private const val ITERATIONS = 100_000

        /** Length of the random salt in bytes */
        private const val SALT_LENGTH = 16

        /** Length of the derived key in bits */
        private const val KEY_LENGTH = 256

        /**
         * Selects an available PBKDF2 algorithm based on platform support.
         *
         * Attempts to use PBKDF2WithHmacSHA256 first, falling back to
         * PBKDF2WithHmacSHA1 if the preferred algorithm is not available.
         *
         * @return The name of an available PBKDF2 algorithm
         * @throws java.security.NoSuchAlgorithmException if neither algorithm is available
         */
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

        /**
         * Hashes a password using PBKDF2 with a randomly generated salt.
         *
         * The function generates a cryptographically secure random salt and applies
         * PBKDF2 key derivation with the available algorithm (HMAC-SHA256 or HMAC-SHA1).
         * The resulting hash is encoded with Base64 along with the algorithm name,
         * iteration count, and salt for storage.
         *
         * @param password The plaintext password to hash
         * @return A Base64-encoded string containing the algorithm, iterations, salt,
         *         and hash in the format: "algorithm:iterations:salt:hash"
         * @throws java.security.NoSuchAlgorithmException if PBKDF2 is not available
         * @throws java.security.spec.InvalidKeySpecException if the key spec is invalid
         *
         * @sample
         * ```
         * val hashedPassword = PasswordHashing.hashPassword("mySecurePassword123")
         * // Result: "PBKDF2WithHmacSHA256:100000:base64Salt:base64Hash"
         * ```
         */
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

        /**
         * Verifies an input password against a stored hash.
         *
         * This function parses the stored hash to extract the algorithm, iterations,
         * salt, and expected hash. It then derives a hash from the input password
         * using the same parameters and compares them in constant time to prevent
         * timing attacks.
         *
         * The function supports two hash formats:
         * - Current format: "algorithm:iterations:salt:hash" (4 parts)
         * - Legacy format: "iterations:salt:hash" (3 parts, assumes PBKDF2WithHmacSHA256)
         *
         * If the stored algorithm is not available, the function attempts to fall back
         * to other available algorithms for maximum compatibility.
         *
         * @param inputPassword The plaintext password to verify
         * @param storedHash The Base64-encoded hash string from storage
         * @return `true` if the password matches the hash, `false` otherwise
         *
         * @sample
         * ```
         * val storedHash = PasswordHashing.hashPassword("myPassword")
         * val isValid = PasswordHashing.verifyPassword("myPassword", storedHash)
         * // isValid: true
         *
         * val isInvalid = PasswordHashing.verifyPassword("wrongPassword", storedHash)
         * // isInvalid: false
         * ```
         */
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
                return false
            }
        }

        /**
         * Compares two byte arrays in constant time to prevent timing attacks.
         *
         * This function ensures that the comparison time does not depend on where
         * the arrays differ, which prevents attackers from using timing information
         * to deduce information about the stored hash.
         *
         * The implementation uses bitwise XOR to compare all bytes, accumulating
         * differences in a result variable. The function always examines every byte,
         * regardless of where differences are found.
         *
         * @param a The first byte array to compare
         * @param b The second byte array to compare
         * @return `true` if the arrays are identical, `false` otherwise
         */
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
