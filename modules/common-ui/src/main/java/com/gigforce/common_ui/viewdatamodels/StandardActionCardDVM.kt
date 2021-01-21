package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDataViewObject

open class StandardActionCardDVM : SimpleDataViewObject {
    var image: Any? = null
    var title: String = ""
    var subtitle: String = ""
    var action: String = ""
    var secondAction: String = ""
    constructor(
        image: Any?,
        title: String,
        subtitle: String,
        action: String,
        secondAction: String
    ) : super(CommonViewTypes.VIEW_STANDARD_ACTION_CARD) {
        this.image = image
        this.title = title
        this.subtitle = subtitle
        this.action = action
        this.secondAction = secondAction
    }

    constructor(
        image: Any?,
        title: String,
        subtitle: String,
        action: String,
        secondAction: String,
        viewType: Int
    ) : super(viewType) {
        this.image = image
        this.title = title
        this.subtitle = subtitle
        this.action = action
        this.secondAction = secondAction
    }
}