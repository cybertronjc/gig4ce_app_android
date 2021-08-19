package com.gigforce.common_ui.viewdatamodels

import android.os.Parcelable
import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes
import kotlinx.android.parcel.Parcelize

@Parcelize
class FeatureItemCard2DVM(
    val active: Boolean = true,
    val borderCorner: Long? = 0,
    val borderShadowRadius: Long? = 0,
    val borderWidth: Long? = 0,
    val borderWidthColor: String? = "",
    val backgroundColor: String? = "",
    val title: String,
    val index: Long = 1000,
    val icon: String? = "",
    val imageRes: Int? = null,
    val imageUrl: String? = null,
    val navPath: String? = null,
    val type: String? = null,
    val parentIcon: String? = null
) :
    SimpleDVM(CommonViewTypes.VIEW_FEATURE_ITEM_CARD2, navPath), Parcelable