package com.example.sportsorganizer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sportsorganizer.data.local.entities.User
import com.example.sportsorganizer.data.repository.UserRepository
import com.example.sportsorganizer.utils.PasswordHashing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing user registration functionality.
 *
 * Handles user creation with password hashing and provides
 * observable state for UI feedback during the registration process.
 *
 * @property userRepository Repository for user data operations
 */
class CreateUserViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {
    /**
     * Sealed class representing the state of user creation.
     */
    sealed class CreationResult {
        /** Initial idle state before any action */
        data object Idle : CreationResult()

        /** Loading state while creating the user */
        data object Loading : CreationResult()

        /**
         * Success state after user creation.
         *
         * @property userId The ID of the created user (0 if not retrieved)
         */
        data class Success(
            val userId: Long,
        ) : CreationResult()

        /**
         * Error state if user creation fails.
         *
         * @property message Human-readable error message
         */
        data class Error(
            val message: String,
        ) : CreationResult()
    }

    private val _creationResult: MutableStateFlow<CreationResult> =
        MutableStateFlow(CreationResult.Idle)

    /**
     * Observable state flow of user creation result.
     *
     * Emits [CreationResult] states as the creation process progresses.
     */
    val creationResult: StateFlow<CreationResult> = _creationResult

    /**
     * Creates a new user with the provided credentials.
     *
     * Hashes the password before storing and updates [creationResult]
     * with the outcome of the operation.
     *
     * @param firstName User's first name (optional)
     * @param lastName User's last name (optional)
     * @param username User's chosen username
     * @param password User's password (will be hashed before storage)
     */
    fun createUser(
        firstName: String?,
        lastName: String?,
        username: String,
        password: String,
    ) {
        _creationResult.value = CreationResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // We let Supabase generate the ID, or we can generate one.
                // Since our Entity has id = 0 default, Supabase should handle it if we omit it or send 0 (if configured).
                // However, our User entity has `val id: Long = 0`.

                val hashedPassword = PasswordHashing.hashPassword(password)

                val user =
                    User(
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        passwordHash = hashedPassword,
                    )
                userRepository.createUser(user)
                _creationResult.value = CreationResult.Success(0)
            } catch (e: Exception) {
                _creationResult.value = CreationResult.Error(e.message ?: "Unknown error")
            }
        }
    }
}

class CreateUserViewModelFactory(
    private val userRepository: UserRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateUserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateUserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
