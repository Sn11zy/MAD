package com.example.sportsorganizer.data.repository

import com.example.sportsorganizer.data.local.entities.User
import com.example.sportsorganizer.data.remote.SupabaseModule
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {

    private val client = SupabaseModule.client

    suspend fun createUser(user: User) {
        withContext(Dispatchers.IO) {
            client.from("users").insert(user)
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        return withContext(Dispatchers.IO) {
            client.from("users").select {
                filter {
                    eq("username", username)
                }
            }.decodeSingleOrNull<User>()
        }
    }

    suspend fun getUserById(id: Long): User? {
        return withContext(Dispatchers.IO) {
            client.from("users").select {
                filter {
                    eq("id", id)
                }
            }.decodeSingleOrNull<User>()
        }
    }
}

