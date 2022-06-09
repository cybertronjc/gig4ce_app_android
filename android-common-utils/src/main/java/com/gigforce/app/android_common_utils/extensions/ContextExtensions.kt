package com.gigforce.app.android_common_utils.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect

import android.os.Vibrator


@SuppressLint("MissingPermission")
fun Context.vibrate(
    durationInMillis: Long = 200
) {
    val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v.vibrate(
            VibrationEffect.createOneShot(
                durationInMillis,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    } else {
        //deprecated in API 26
        v.vibrate(durationInMillis)
    }
}