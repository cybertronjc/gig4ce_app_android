package com.gigforce.modules.feature_chat.ui.chatItems

enum class MessageFlowType constructor(
    private val flowType : String
){

    IN("in"),
    OUT("out");

    override fun toString(): String {
        return flowType
    }
}

enum class MessageType {
    ONE_TO_ONE_MESSAGE,
    GROUP_MESSAGE;
}