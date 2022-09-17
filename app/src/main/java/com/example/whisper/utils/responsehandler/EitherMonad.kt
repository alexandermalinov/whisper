package com.example.whisper.utils.responsehandler

interface EitherMonad {

    fun isLeft(): Boolean

    fun isRight(): Boolean
}

interface LeftEither : EitherMonad {

    override fun isLeft() = true

    override fun isRight() = false
}

interface RightEither : EitherMonad {

    override fun isLeft() = false

    override fun isRight() = true
}

/**
 * Compose 2 functions.
 * See https://proandroiddev.com/kotlins-nothing-type-946de7d464fb.
 * Credits to Alex Hart.
 */
fun <A, B, C> ((A) -> B).c(f: (B) -> C): (A) -> C = { f(this(it)) }

/**
 * Right-biased map(). Right is assumed to be the default case to operate on.
 * If it is Left, operations as map and flatMap return the Left value unchanged
 */
fun <T, L, R> Either<L, R>.map(fn: (R) -> (T)): Either<L, T> = this.flatMap(fn.c { t ->
    Either.right(t)
})

/**
 * Returns the value from Right or the given argument if this is a Left.
 * Right(1).gerOrElse(2) returns 1.
 * Left(1).getOrElse(2) return 2.
 */
fun <L, R> Either<L, R>.getOrElse(value: R): R = when (this) {
    is Either.Left -> value
    is Either.Right -> r
}