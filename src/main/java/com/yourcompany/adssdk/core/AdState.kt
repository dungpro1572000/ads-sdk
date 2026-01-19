package com.yourcompany.adssdk.core

/**
 * Sealed class đại diện cho trạng thái của Ad
 */
sealed class AdState {
    object Idle : AdState()
    object Loading : AdState()
    object Loaded : AdState()
    object Showing : AdState()
    data class Error(val error: AdError) : AdState()
}

/**
 * Sealed class cho các loại lỗi
 */
sealed class AdError(
    open val code: Int,
    open val message: String,
    open val providerErrorCode: Int? = null
) {
    data class LoadError(
        override val code: Int,
        override val message: String,
        override val providerErrorCode: Int? = null
    ) : AdError(code, message, providerErrorCode)

    data class ShowError(
        override val code: Int,
        override val message: String,
        override val providerErrorCode: Int? = null
    ) : AdError(code, message, providerErrorCode)

    data class NetworkError(
        override val message: String = "No internet connection"
    ) : AdError(code = -1, message = message)

    data class NotInitializedError(
        override val message: String = "SDK not initialized"
    ) : AdError(code = -2, message = message)

    data class AdNotReadyError(
        override val message: String = "Ad not loaded yet"
    ) : AdError(code = -3, message = message)

    companion object {
        const val ERROR_CODE_NO_FILL = 1
        const val ERROR_CODE_NETWORK = 2
        const val ERROR_CODE_INVALID_REQUEST = 3
        const val ERROR_CODE_INTERNAL = 4
    }
}
