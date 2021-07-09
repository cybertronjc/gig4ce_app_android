package com.gigforce.common_ui.viewdatamodels

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDVM
import com.google.android.exoplayer2.video.spherical.Projection

data class SimpleCardDVM(val title : String = "", val subtitle:String = "", val image : Int, val navpath: String = "", var isSelected : Boolean = false):SimpleDVM(CommonViewTypes.VIEW_SIMPLE_CARD) {
}