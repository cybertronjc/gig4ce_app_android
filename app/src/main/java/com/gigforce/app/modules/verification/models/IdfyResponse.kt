package com.gigforce.app.modules.verification.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class IdfyResponse (

    @SerializedName("action") val action : String,
    @SerializedName("completed_at") val completed_at : String,
    @SerializedName("created_at") val created_at : String,
    @SerializedName("group_id") val group_id : String,
    @SerializedName("request_id") val request_id : String,
    @SerializedName("result") val result : Result,
    @SerializedName("status") val status : String,
    @SerializedName("task_id") val task_id : String,
    @SerializedName("type") val type : String
)

data class Result (

    @SerializedName("extraction_output") val extraction_output : Extraction_output,
    @SerializedName("qr_output") val qr_output : Qr_output
)

data class Qr_output (

    @SerializedName("address") val address : String,
    @SerializedName("date_of_birth") val date_of_birth : String,
    @SerializedName("district") val district : String,
    @SerializedName("gender") val gender : String,
    @SerializedName("house_number") val house_number : String,
    @SerializedName("id_number") val id_number : String,
    @SerializedName("name_on_card") val name_on_card : String,
    @SerializedName("pincode") val pincode : String,
    @SerializedName("state") val state : String,
    @SerializedName("street_address") val street_address : String,
    @SerializedName("year_of_birth") val year_of_birth : String
)

data class Extraction_output (

    @SerializedName("address") val address : String,
    @SerializedName("date_of_birth") val date_of_birth : String,
    @SerializedName("district") val district : String,
    @SerializedName("fathers_name") val fathers_name : String,
    @SerializedName("gender") val gender : String,
    @SerializedName("house_number") val house_number : String,
    @SerializedName("id_number") val id_number : Int,
    @SerializedName("is_scanned") val is_scanned : Boolean,
    @SerializedName("name_on_card") val name_on_card : String,
    @SerializedName("pincode") val pincode : Int,
    @SerializedName("state") val state : String,
    @SerializedName("street_address") val street_address : String,
    @SerializedName("year_of_birth") val year_of_birth : Int
)

//    "extraction_output": {
//    "address": "S/O Kakaiah Cindula, G4 Block B Ge Ascentia Hitex Road, Khanamet Kothaguda, Kondapur, K.v. Rangare Telangana - 500084",
//    "date_of_birth": "1986-08-19",
//    "district": null,
//    "fathers_name": "Kakaiah Cindula",
//    "gender": "MALE",
//    "house_number": "G4",
//    "id_number": "714054258465",
//    "is_scanned": "false",
//    "name_on_card": null,
//    "pincode": "500084",
//    "state": "Telangana",
//    "street_address": "Block B Ge Ascentia Hitex Road, Khanamet Kothaguda, Kondapur, K.v. Rangare",
//    "year_of_birth": "1986"
//    }
//
//
//{
//    "action": "extract",
//    "completed_at": "2020-04-06T21:40:41+05:30",
//    "created_at": "2020-04-06T21:40:28+05:30",
//    "group_id": "8e16424a-58fc-4ba4-ab20-5bc8e7c3c41f",
//    "request_id": "cea24e54-aedc-4a06-b4af-d378f57e999d",
//    "result": {
//    "extraction_output": {
//        "address": "S/O Kakaiah Cindula, G4 Block B Ge Ascentia Hitex Road, Khanamet Kothaguda, Kondapur, K.v. Rangare Telangana - 500084",
//        "date_of_birth": "1986-08-19",
//        "district": null,
//        "fathers_name": "Kakaiah Cindula",
//        "gender": "MALE",
//        "house_number": "G4",
//        "id_number": "714054258465",
//        "is_scanned": "false",
//        "name_on_card": null,
//        "pincode": "500084",
//        "state": "Telangana",
//        "street_address": "Block B Ge Ascentia Hitex Road, Khanamet Kothaguda, Kondapur, K.v. Rangare",
//        "year_of_birth": "1986"
//    },
//    "qr_output": {
//        "address": null,
//        "date_of_birth": null,
//        "district": null,
//        "gender": null,
//        "house_number": null,
//        "id_number": null,
//        "name_on_card": null,
//        "pincode": null,
//        "state": null,
//        "street_address": null,
//        "year_of_birth": null
//    }
//},
//    "status": "completed",
//    "task_id": "74f4c926-250c-43ca-9c53-453e87ceacd2",
//    "type": "ind_aadhaar"
//}