package com.example.sportsorganizer.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

/**
 * Singleton module providing the configured Supabase client.
 *
 * This object initializes and configures the Supabase client with:
 * - Postgrest for database operations
 * - Auth for authentication (if needed)
 * - KotlinX Serialization with ignore unknown keys option
 *
 * The client is used across all repository classes to interact with
 * the remote Supabase database.
 */
object SupabaseModule {
    /**
     * The configured Supabase client instance.
     *
     * Provides access to all Supabase features including database queries,
     * authentication, and real-time subscriptions.
     */
    val client =
        createSupabaseClient(
            supabaseUrl = "https://kchvkddazrbhcpspthal.supabase.co/",
            supabaseKey = "sb_secret_Xz9qeFwvAR_SH1SeqYgcsg_yDEs37-s",
        ) {
            install(Postgrest)
            install(Auth)

            defaultSerializer =
                KotlinXSerializer(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
        }
}
