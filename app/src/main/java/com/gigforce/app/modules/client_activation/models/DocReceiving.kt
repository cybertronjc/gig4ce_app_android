package com.gigforce.app.modules.client_activation.models

data class DocReceiving(var title:String="",var subtitle:String="",var checkItems: List<CheckItem> = listOf(), var jobProfileId: String = "", var type: String = "")