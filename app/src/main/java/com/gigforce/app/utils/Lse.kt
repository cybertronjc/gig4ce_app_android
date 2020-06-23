package com.gigforce.app.utils

/**
 * Lse (Loading-Success-Error Class for sharing state between viewModel & view)
 */
sealed class Lse {

    object Loading : Lse()
    object Success : Lse()
    class Error(val error: String) : Lse()

    companion object {

        fun success(): Lse = Success
        fun loading(): Lse = Loading
        fun error(error: String): Lse = Error(error)
    }
}