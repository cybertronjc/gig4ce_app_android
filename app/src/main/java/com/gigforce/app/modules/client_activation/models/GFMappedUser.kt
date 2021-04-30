package com.gigforce.app.modules.client_activation.models

data class GFMappedUser(var city: String? = null, var email: String? = null, var name: String? = null, var number: String = "", var numberWithoutnineone:String = "",var numbers:List<String> = ArrayList<String>()) {}