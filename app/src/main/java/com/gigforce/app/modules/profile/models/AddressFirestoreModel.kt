package com.gigforce.app.modules.profile.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.firestore.Exclude

class AddressFirestoreModel: BaseFirestoreDataModel {
    var current:AddressModel=AddressModel()
    var home:AddressModel= AddressModel()
    constructor(
        current:AddressModel = AddressModel(),
        home:AddressModel = AddressModel()
    ):super("address"){
        this.current=current
        this.home=home
    }
    constructor() : super("address") {

    }

    @Exclude
    fun isCurrentAddressAndPermanentAddressTheSame() : Boolean {
        if(current.isEmpty())
            return false

        return current.city == home.city && current.state == home.state
    }
}