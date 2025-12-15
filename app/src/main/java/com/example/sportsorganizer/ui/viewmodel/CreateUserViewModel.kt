package com.example.sportsorganizer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sportsorganizer.data.local.daos.UserDao
import com.example.sportsorganizer.data.local.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateUserViewModel(
    private val userDao: UserDao,
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
                val newUserId: Long = System.currentTimeMillis()
                val user =
                    User(
                        id = newUserId,
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        password = password,
                    )
                userDao.insertAll(user)
                _creationResult.value = CreationResult.Success(newUserId)
            } catch (e: Exception) {
                _creationResult.value = CreationResult.Error(e.message ?: "Unknown error")
            }
        }
    }
}

class CreateUserViewModelFactory(
    private val userDao: UserDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateUserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateUserViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
