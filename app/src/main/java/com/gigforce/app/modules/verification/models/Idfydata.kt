package com.gigforce.app.modules.verification.models

data class Idfydata(
    val id: Int,
    val address: String,
    val date_of_birth: String,
    val fathers_name:String,
    val is_scanned: Boolean
)

//{"address":null,"date_of_birth":null,"district":null,"fathers_name":null,"gender":"MALE","house_number":null,"id_number":null,"is_scanned":"false","name_on_card":"SMITH STEVE PETER","pincode":null,"state":null,"street_address":null,"year_of_birth":"1989"},

data class IdfydataResponse(
    val results: List<Idfydata>
)