package com.gigforce.app.core.base.basefirestore.example.model

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

class ContactFirestoreDataModel:
    BaseFirestoreDataModel {
    constructor(
        phone: String = "",
        email: String = ""
    ):super("Contact")
}