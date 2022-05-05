package com.gigforce.core.logger

import timber.log.Timber
import javax.inject.Inject

class GigforceLogger @Inject constructor() {

     fun v(
        tag: String,
        message: String,
        vararg args: Any
    ) {
         try {
             Timber.tag(tag)
             Timber.v(message, args)
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
             Timber.d(message, args)
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
        e : Throwable
    ) {

        try {
            Timber.tag(tag)
            Timber.d(e,message)
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
             Timber.e(e, occurredWhen,args)
         } catch (e: Exception) {
             e.printStackTrace()
         }
     }

}