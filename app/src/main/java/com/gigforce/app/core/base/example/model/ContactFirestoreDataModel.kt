package com.gigforce.app.core.base.example.model

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel

class ContactFirestoreDataModel:
    BaseFirestoreDataModel {
    constructor(
        phone: String = "",
        email: String = ""
    ):super("Contact")
}