package com.gigforce.core.logger

import timber.log.Timber
import javax.inject.Inject

class GigforceLogger @Inject constructor() {

     fun v(
        tag: String,
        message: String,
        vararg args: Any
    ) {
        Timber.tag(tag)
        Timber.v(message, args)
    }

     fun d(
        tag: String,
        message: String,
        vararg args: Any
    ) {
        Timber.tag(tag)
        Timber.d(message, args)
    }

     fun d(
        tag: String,
        message: String
    ) {
        Timber.tag(tag)
        Timber.d(message)
    }

     fun e(
        tag: String,
        occurredWhen: String,
        e: Throwable
    ) {
        Timber.tag(tag)
        Timber.e(e, occurredWhen)
    }

     fun e(
        tag: String,
        occurredWhen: String,
        e: Throwable,
        vararg args: Any
    ) {
        Timber.tag(tag)
        Timber.e(e, occurredWhen,args)
    }

}