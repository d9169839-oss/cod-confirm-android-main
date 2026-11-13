package com.codconfirm.app.core.common

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    data object NotFound : AppError
    data object InvalidStatusTransition : AppError
    data object Storage : AppError
    data class Validation(val field: String, val reason: String) : AppError
    data class Unknown(val cause: Throwable? = null) : AppError
}
