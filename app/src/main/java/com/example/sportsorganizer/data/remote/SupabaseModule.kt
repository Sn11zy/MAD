package com.example.sportsorganizer.data.remote

import com.example.sportsorganizer.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseModule {
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
