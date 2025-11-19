package com.example.sportsorganizer.data.repository

import android.content.Context
import com.example.sportsorganizer.data.local.daos.UserDao
import com.example.sportsorganizer.data.local.entities.User
import com.example.sportsorganizer.data.local.session.SessionManager
import com.example.sportsorganizer.utils.PasswordHashing

class AuthRepository(
    private val userDao: UserDao,
    context: Context,
) {
    private val sessionManager = SessionManager(context)

    suspend fun login(
        username: String,
        password: String,
    ): kotlin.Result<User> {
        val user = userDao.findByUsername(username)
        return if (user != null && PasswordHashing.verifyPassword(password, user.password)) {
            sessionManager.saveLoggedInUserId(user.id)
            kotlin.Result.success(user)
        } else {
            kotlin.Result.failure(Exception("Invalid credentials"))
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }

    suspend fun currentUser(): User? {
        val id = sessionManager.getLoggedInUserId() ?: return null
        val users = userDao.loadAllByIds(longArrayOf(id))
        return users.firstOrNull()
    }
}
