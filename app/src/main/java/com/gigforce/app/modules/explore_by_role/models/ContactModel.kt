package com.gigforce.app.modules.explore_by_role.models

import com.gigforce.app.modules.profile.models.ContactEmail
import com.gigforce.app.modules.profile.models.ContactPhone

data
class ContactModel(var contactPhone: ContactPhone? = ContactPhone(), var contactEmail: ContactEmail? = ContactEmail(),
                   var validateFields:Boolean=false) {
}