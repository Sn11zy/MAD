package com.example.sportsorganizer.data.repository

import com.example.sportsorganizer.data.local.entities.User
import com.example.sportsorganizer.data.remote.SupabaseModule
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {
    private val client = SupabaseModule.client

    /**
     * Creates a new user in the database.
     *
     * @param user The [User] object to create.
     */
    suspend fun createUser(user: User) {
        withContext(Dispatchers.IO) {
            client.from("users").insert(user)
        }
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username The username to search for.
     * @return The [User] object if found, null otherwise.
     */
    suspend fun getUserByUsername(username: String): User? =
        withContext(Dispatchers.IO) {
            client
                .from("users")
                .select {
                    filter {
                        eq("username", username)
                    }
                }.decodeSingleOrNull<User>()
        }

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user.
     * @return The [User] object if found, null otherwise.
     */
    suspend fun getUserById(id: Long): User? =
        withContext(Dispatchers.IO) {
            client
                .from("users")
                .select {
                    filter {
                        eq("id", id)
                    }
                }.decodeSingleOrNull<User>()
        }
}
