package com.gigforce.app.modules.client_activation.models

data class QuestionnairAnswerModel(var options:ArrayList<QuestionnairOptionalModel>?=null,var type:String?=null, var selectedDropdownValue:String?=null) {
}