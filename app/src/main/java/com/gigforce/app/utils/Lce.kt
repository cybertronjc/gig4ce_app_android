package com.gigforce.app.utils

/**
 * Lce (Loading-Content-Error) Class for sharing state between viewModel & view
 */
sealed class Lce<out T> {

    object Loading : Lce<Nothing>()
    class Content<out T>(val content: T) : Lce<T>()
    class Error<out T>(val error: String) : Lce<T>()

    companion object {

        fun <T> content(content: T): Lce<T> = Content(content)
        fun <T> loading(): Lce<T> = Loading
        fun <T> error(error: String): Lce<T> = Error(error)
    }
}