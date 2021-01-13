package com.gigforce.app.modules.chatmodule

sealed class ChatAttachmentDownloadState

data class DownloadStarted(val index : Int) : ChatAttachmentDownloadState()
data class DownloadCompleted(val index : Int) : ChatAttachmentDownloadState()
data class ErrorWhileDownloadingAttachment(val index : Int,val  error : String) : ChatAttachmentDownloadState()