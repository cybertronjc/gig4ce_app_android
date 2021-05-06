package com.gigforce.app.modules.explore_by_role.models

import com.gigforce.core.datamodels.profile.ContactEmail
import com.gigforce.core.datamodels.profile.ContactPhone

data
class ContactModel(var contactPhone: ContactPhone? = ContactPhone(), var contactEmail: ContactEmail? = ContactEmail(),
                   var validateFields:Boolean=false) {
}