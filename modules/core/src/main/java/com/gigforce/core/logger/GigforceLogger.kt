package com.gigforce.core.logger

import timber.log.Timber
import javax.inject.Inject

class GigforceLogger @Inject constructor() {

    fun v(
        tag: String,
        message: String
    ) {
        try {
            Timber.tag(tag)
            Timber.v(
                message
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun v(
        tag: String,
        message: String,
        vararg args: Any
    ) {
        try {
            Timber.tag(tag)
            Timber.v(
                appendMessagesAndArguments(
                    message,
                    args
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun d(
        tag: String,
        message: String,
        vararg args: Any
    ) {
        try {
            Timber.tag(tag)
            Timber.d(
                appendMessagesAndArguments(
                    message,
                    args
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun w(
        tag: String,
        message: String
    ) {

        try {
            Timber.tag(tag)
            Timber.w(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun d(
        tag: String,
        message: String
    ) {

        try {
            Timber.tag(tag)
            Timber.d(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun d(
        tag: String,
        message: String,
        e: Throwable
    ) {

        try {
            Timber.tag(tag)
            Timber.d(e, message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun e(
        tag: String,
        occurredWhen: String,
        e: Throwable
    ) {
        try {
            Timber.tag(tag)
            Timber.e(e, occurredWhen)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun e(
        tag: String,
        occurredWhen: String,
        e: Throwable,
        vararg args: Any
    ) {
        try {
            Timber.tag(tag)
            Timber.e(
                e, appendMessagesAndArguments(
                    occurredWhen,
                    args
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun appendMessagesAndArguments(
        message: String,
        vararg args: Any
    ) = buildString {
        append(message)
        append("\n")
        append(convertArgsToString(args))
    }


    private fun convertArgsToString(
        vararg args: Any
    ) = buildString {
        append("Arguments :-\n")

        args.forEachIndexed { index, any ->
            append(index)
            append(" : ")
            append(convertAnyToStringOrNull(args))
        }
    }


    private fun convertAnyToStringOrNull(
        any: Any
    ): String? {

        try {

            return if (any is Array<*> && any.isNotEmpty()) {
                convertAnyToStringOrNull(
                    any[0]!!
                )
            } else {
                any.toString()
            }
        } catch (e: Exception) {
            return null
        }
    }

}