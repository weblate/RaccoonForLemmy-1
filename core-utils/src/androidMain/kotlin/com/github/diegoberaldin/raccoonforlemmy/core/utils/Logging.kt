package com.github.diegoberaldin.raccoonforlemmy.core.utils

actual object Log {

    private const val TAG = "com.github.diegoberaldin.raccoonforlemmy"
    actual fun d(message: String) {
        android.util.Log.d(TAG, message)
    }
}