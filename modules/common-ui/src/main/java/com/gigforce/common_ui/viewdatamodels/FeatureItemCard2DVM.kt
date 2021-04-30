package com.gigforce.common_ui.viewdatamodels

import android.os.Parcelable
import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDVM
import kotlinx.android.parcel.Parcelize

@Parcelize
class FeatureItemCard2DVM(
    val title : String,
    val index:Int = 0,
    val image_type: String?,
    val imageRes:Int? = null,
    val imageUrl:String? = null,
    val navPath:String? = null) :
    SimpleDVM(CommonViewTypes.VIEW_FEATURE_ITEM_CARD2, navPath), Parcelable {
}