package com.gigforce.app.modules.questionnaire.models

import android.os.Parcel
import android.os.Parcelable

data class Options(@field:JvmField var isAnswer: Boolean = false, var question: String = "", var type: String = "")