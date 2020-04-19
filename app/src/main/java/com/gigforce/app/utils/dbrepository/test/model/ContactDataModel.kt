package com.gigforce.app.utils.dbrepository.test.model

import com.gigforce.app.utils.dbrepository.BaseDataModel

class ContactDataModel:BaseDataModel{
    constructor(
        phone: String = "",
        email: String = ""
    ):super("Contact")
}