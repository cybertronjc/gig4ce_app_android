package com.gigforce.core.utils

import android.os.Bundle

interface NavFragmentsData {
    fun setData(bundle: Bundle)
    fun getData(): Bundle
}