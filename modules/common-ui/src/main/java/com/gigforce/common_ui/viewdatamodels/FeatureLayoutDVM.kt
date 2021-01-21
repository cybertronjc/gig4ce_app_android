package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDataViewObject

class FeatureLayoutDVM (val image : Any, val title : String, val featuredData : List<Any>): SimpleDataViewObject(CommonViewTypes.VIEW_FEATURE_LAYOUT) {
}