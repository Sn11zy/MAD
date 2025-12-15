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

class CreateUserViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {
    sealed class CreationResult {
        data object Idle : CreationResult()

        data object Loading : CreationResult()

        data class Success(
            val userId: Long,
        ) : CreationResult()

        data class Error(
            val message: String,
        ) : CreationResult()
    }

    private val _creationResult: MutableStateFlow<CreationResult> =
        MutableStateFlow(CreationResult.Idle)
    val creationResult: StateFlow<CreationResult> = _creationResult

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
                
                val user = User(
                    firstName = firstName,
                    lastName = lastName,
                    username = username,
                    passwordHash = hashedPassword
                )
                userRepository.createUser(user)
                _creationResult.value = CreationResult.Success(0) // We don't get the ID back easily here without fetch, assuming success
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
