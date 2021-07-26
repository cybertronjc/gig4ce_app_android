package com.gigforce.common_ui.viewdatamodels

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class GigerProfileCardDVM(
    val gigerImg: String,
    val name: String,
    var number: String,
    var jobProfileName: String,
    val jobProfileLogo: String
) : Parcelable