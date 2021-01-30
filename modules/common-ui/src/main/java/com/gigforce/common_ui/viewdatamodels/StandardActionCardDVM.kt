package com.gigforce.common_ui.viewdatamodels

import android.os.Parcelable
import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDataViewObject
import kotlinx.android.parcel.Parcelize

@Parcelize
open class StandardActionCardDVM(var image: Int?,
                                 var title: String,
                                 var subtitle: String,
                                 var action: String,
                                 var secondAction: String,
                                 val defaultViewType:Int = CommonViewTypes.VIEW_STANDARD_ACTION_CARD
) :
    SimpleDataViewObject(defaultViewType),
    Parcelable {}