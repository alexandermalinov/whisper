package com.example.whisper.utils.responsehandler

import androidx.annotation.StringRes
import com.example.whisper.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Base error model that has to be extended by all other errors
 */
sealed class ErrorModel

/**
 * Base API error model that has to be extended by all other api errors
 */
sealed class ApiError : ErrorModel()

/**
 * Generic all-purpose http error model for all API errors
 */
data class HttpError(
    @StringRes
    val errorMessageRes: Int? = R.string.error_something_went_wrong,
    val errorCode: Int? = null,
    val errorMessage: String? = null,
    val serverMessage: String? = null
) : ApiError()

/**
 * Network error model
 */
data class NetworkError(
    @StringRes
    val errorMessage: Int = R.string.error_network
) : ApiError()

/**
 * Model wrapping the response from the server
 */
@Serializable
data class ErrorResponseModel(
    @SerialName("message")
    val errorMessage: String? = null,
    @SerialName("error")
    val error: String? = null
)

fun ErrorResponseModel.toApiError(errorCode: Int?) = HttpError(
    errorMessage = errorMessage,
    serverMessage = error,
    errorCode = errorCode
)