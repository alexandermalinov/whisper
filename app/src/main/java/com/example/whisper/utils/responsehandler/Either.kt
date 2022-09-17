package com.example.whisper.utils.responsehandler

sealed class Either<out L, out R> : EitherMonad {

    /* --------------------------------------------------------------------------------------------
     * Exposed
    ---------------------------------------------------------------------------------------------*/
    fun <X> fold(
        fl: (L) -> X,
        fr: (R) -> X
    ): X = when (this) {
        is Left -> fl(l)
        is Right -> fr(r)
    }

    suspend fun <X> foldSuspend(
        fl: suspend (L) -> X,
        fr: suspend (R) -> X
    ): X = when (this) {
        is Left -> fl(l)
        is Right -> fr(r)
    }

    fun <T, L, R> Either<L, R>.flatMap(fn: (R) -> Either<L, T>): Either<L, T> = when (this) {
        is Left -> Left(l)
        is Right -> fn(r)
    }

    fun <T, L, R> Either<L, R>.map(fn: (R) -> (T)): Either<L, T> = this.flatMap(fn.c(Companion::right))

    /* --------------------------------------------------------------------------------------------
     * Inner Classes
    ---------------------------------------------------------------------------------------------*/
    data class Left<out L, out R>(val l: L) : Either<L, R>(), LeftEither

    data class Right<out L, out R>(val r: R) : Either<L, R>(), RightEither

    /* --------------------------------------------------------------------------------------------
     * Static
    ---------------------------------------------------------------------------------------------*/
    companion object {
        fun <L> left(left: L): Left<L, Nothing> = Left(left)
        fun <R> right(right: R): Right<Nothing, R> = Right(right)
    }
}