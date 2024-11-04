package com.example.deepwork.domain.model

import androidx.annotation.StringRes

sealed class Result<out T> {

    companion object {
        /**
         * Wraps the lambda in a try catch, returning the last expression as Success
         * or returning Error if an exception was thrown.
         */
        fun <T> of(action: () -> T): Result<T> {
            return try {
                Success(action())
            } catch (e: Exception) {
                Error(e)
            }
        }
    }
    /**
     * Represents a successful operation. Contains the result of the successful operation.
     * @property value The result of the operation.
     */
    data class Success<out T>(val value: T) : Result<T>()

    /**
     * Represents a failed operation. Contains the exception that was thrown during the operation.
     * @property exception The exception thrown during the operation.
     * @property message The error message as a String resource.
     */
    data class Error(
        val exception: Throwable,
        @StringRes val message: Int? = null
    ) : Result<Nothing>()

    /** Returns true if the Result is a Success, false otherwise. */
    val isSuccess: Boolean get() = this is Success

    /** Returns true if the Result is an Error, false otherwise. */
    val isError: Boolean get() = this is Error

    /**
     * Performs the given [action] when the Result is a Success.
     * @param action A lambda that is invoked with the success value when the Result is a Success.
     * @return The original Result for chainability.
     */
    fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) {
            action(value)
        }
        return this
    }

    /**
     * Performs the given [action] when the Result is an Error.
     * @param action A lambda that is invoked with the exception when the Result is an Error.
     * @return The original Result for chainability.
     */
    fun onError(action: (Throwable, Int?) -> Unit): Result<T> {
        if (this is Error) {
            action(exception, message)
        }
        return this
    }

    /**
     * Transforms the value contained in this Result if it is a Success, otherwise returns the original Result.
     * @param transform A function to apply to the value if this Result is a Success.
     * @return The transformed Result if this Result is a Success, otherwise the original Result.
     */
    fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(value))
            is Error -> Error(exception)
        }
    }

    /**
     * Gets the value of this Result if it is a Success, otherwise throws the exception if it is an Error.
     * @return The value of this Result if it is a Success.
     * @throws Throwable if the Result is an Error.
     */
    fun getOrThrow(): T {
        return when (this) {
            is Success -> value
            is Error -> throw exception
        }
    }

    /**
     * Gets the value of this Result if it is a Success, otherwise returns null.
     * @return The value of this Result if it is a Success, otherwise null.
     */
    fun getOrNull(): T? {
        return when (this) {
            is Success -> value
            is Error -> null
        }
    }
}
