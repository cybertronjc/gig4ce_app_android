package com.gigforce.app.modules.client_activation.models

data class DocReceiving(var checkItems: List<CheckItem> = listOf(), var jobProfileId: String = "", var type: String = "")