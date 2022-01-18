package com.gigforce.common_ui.viewdatamodels

import android.os.Bundle
import androidx.core.os.bundleOf
import com.gigforce.core.NavArgs
import com.gigforce.core.SimpleDVM

import com.gigforce.core.datamodels.CommonViewTypes

class SimpleCardDVM1(
    val id : String?="", val label :String?="", val navPath : String?=null
    ): SimpleDVM(
        CommonViewTypes.VIEW_SIMPLE_CARD1
            ){
    override fun getNavArgs(): NavArgs? {
        return NavArgs(args = bundleOf("id" to id,"label" to label),navPath = navPath?:"")
    }
    }