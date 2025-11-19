package com.example.sportsorganizer.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHashingTest {
    @Test
    fun testHashAndVerify_success() {
        val pw = "correct-horse-battery-staple"
        val stored = PasswordHashing.hashPassword(pw)
        assertTrue(PasswordHashing.verifyPassword(pw, stored))
    }

    @Test
    fun testVerify_failIncorrectPassword() {
        val stored = PasswordHashing.hashPassword("right-password")
        assertFalse(PasswordHashing.verifyPassword("wrong-password", stored))
    }
}
