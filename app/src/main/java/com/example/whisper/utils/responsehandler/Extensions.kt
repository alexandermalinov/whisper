package com.example.whisper.utils.responsehandler

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Determines if the [Response] is successful or not.
 * @return Either the result or an error
 */
fun <Result> Response<Result>.getResult() = body().let { body ->
    when {
        isSuccessful && body != null -> Either.right(body)
        isSuccessful -> Either.left(NetworkError())
        else -> Either.left(
            errorBody()
                ?.let { errorBody ->
                    Json.decodeFromString<ErrorResponseModel>(errorBody.string()).toApiError(code())
                }
                ?: NetworkError()
        )
    }
}

/**
 * Executes API calls
 * @param params request params if any
 * @param getResponse service call that returns response
 * @return either [ApiError] or [Result]
 */
suspend inline fun <Result, Param> executeApiCall(
    params: Param,
    noinline getResponse: suspend (Param) -> Response<Result>
): Either<ApiError, Result> = withContext(Dispatchers.Default) {
    try {
        getResponse(params).getResult()
    } catch (e: IOException) {
        Either.left(NetworkError())
    } catch (e: HttpException) {
        Either.left(HttpError())
    } catch (e: Exception) {
        Either.left(HttpError())
    }
}

/**
 * Executes API calls
 * @param getResponse service call that returns response
 * @return either [ApiError] or [Result]
 */
suspend inline fun <Result> executeApiCall(
    noinline getResponse: suspend () -> Response<Result>
): Either<ApiError, Result> = withContext(Dispatchers.Default) {
    try {
        getResponse().getResult()
    } catch (e: IOException) {
        Either.left(NetworkError())
    } catch (e: HttpException) {
        Either.left(HttpError())
    } catch (e: Exception) {
        Either.left(HttpError())
    }
}