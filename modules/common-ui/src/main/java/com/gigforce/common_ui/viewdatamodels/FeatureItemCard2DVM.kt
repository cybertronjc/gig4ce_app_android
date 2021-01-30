package com.gigforce.common_ui.viewdatamodels

import android.os.Parcelable
import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDataViewObject
import kotlinx.android.parcel.Parcelize

@Parcelize
class FeatureItemCard2DVM(
    val image:Int,
    val title : String,
    val navPath:String? = null) :
    SimpleDataViewObject(CommonViewTypes.VIEW_FEATURE_ITEM_CARD2, navPath), Parcelable {
}