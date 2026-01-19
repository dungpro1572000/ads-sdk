package com.yourcompany.adssdk.utils

import android.util.Log

/**
 * Logger utility cho SDK
 */
object AdsLogger {

    private const val TAG = "AdsSDK"
    private var isEnabled = true

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun d(tag: String, message: String) {
        if (isEnabled) {
            Log.d("$TAG:$tag", message)
        }
    }

    fun i(tag: String, message: String) {
        if (isEnabled) {
            Log.i("$TAG:$tag", message)
        }
    }

    fun w(tag: String, message: String) {
        if (isEnabled) {
            Log.w("$TAG:$tag", message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isEnabled) {
            if (throwable != null) {
                Log.e("$TAG:$tag", message, throwable)
            } else {
                Log.e("$TAG:$tag", message)
            }
        }
    }
}
