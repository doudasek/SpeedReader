package com.martindoudera.speedreader.core.common.error

sealed interface Outcome<out T, out E> {
    data class Success<T>(val value: T) : Outcome<T, Nothing>

    data class Failure<E>(val error: E) : Outcome<Nothing, E>
}

inline fun <T, R, E> Outcome<T, E>.map(transform: (T) -> R): Outcome<R, E> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}

inline fun <T, E, F> Outcome<T, E>.mapError(transform: (E) -> F): Outcome<T, F> = when (this) {
    is Outcome.Success -> this
    is Outcome.Failure -> Outcome.Failure(transform(error))
}

/** Runs [action] on the value of a success, e.g. to log it, and returns the outcome unchanged. */
inline fun <T, E> Outcome<T, E>.onSuccess(action: (T) -> Unit): Outcome<T, E> {
    if (this is Outcome.Success) action(value)
    return this
}

/** Runs [action] on the error of a failure, e.g. to log it, and returns the outcome unchanged. */
inline fun <T, E> Outcome<T, E>.onFailure(action: (E) -> Unit): Outcome<T, E> {
    if (this is Outcome.Failure) action(error)
    return this
}

fun <T, E> Outcome<T, E>.valueOrNull(): T? = when (this) {
    is Outcome.Success -> value
    is Outcome.Failure -> null
}
