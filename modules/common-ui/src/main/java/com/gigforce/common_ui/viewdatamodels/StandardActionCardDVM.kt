package com.gigforce.common_ui.viewdatamodels

import android.app.Notification
import android.os.Parcelable
import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDVM
import kotlinx.android.parcel.Parcelize


open class StandardActionCardDVM(val image: Int?,
                                 val imageUrl:String?=null,
                                 val title: String,
                                 var subtitle: String,
                                 var action1: ActionButton?=null,
                                 var action2:ActionButton?=null,
                                 val bgcolor:Int=0,
                                 val marginRequired:Boolean = false,
                                 val defaultViewType:Int = CommonViewTypes.VIEW_STANDARD_ACTION_CARD
) :SimpleDVM(defaultViewType){}


open class ActionButton(val title: String? = "",navPath : String? = ""){}