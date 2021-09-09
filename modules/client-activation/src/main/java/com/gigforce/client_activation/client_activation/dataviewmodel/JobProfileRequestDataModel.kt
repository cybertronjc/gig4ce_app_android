package com.gigforce.client_activation.client_activation.dataviewmodel

data class JobProfileRequestDataModel(
    var text: String? = "",
    var gigerId: String? = "",
    var type: String? = "",
    var sortOrder: Int? = 1,
    var sortBy: String? = "",
    var pageNo: Int? = 1,
    var pageSize: Int? = 15,
) {
}