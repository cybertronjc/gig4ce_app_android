package com.gigforce.common_ui.exceptions

class TryingToDowngradeJoiningStatusException constructor(
    documentId : String,
    val existingStatus : String,
    newStatus : String
) : Exception(
    "trying to downgrade status of exisiting joining ,document id : $documentId," +
    " exiting status : $existingStatus, New status : $newStatus"
){


}