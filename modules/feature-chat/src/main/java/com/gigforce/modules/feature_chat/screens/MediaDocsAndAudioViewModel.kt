package com.gigforce.modules.feature_chat.screens

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.ChatFileManager
import com.gigforce.common_ui.chat.ChatGroupRepository
import com.gigforce.common_ui.chat.models.ChatGroup
import com.gigforce.common_ui.chat.models.GroupMedia
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.modules.feature_chat.models.ChatMediaViewModels
import com.gigforce.modules.feature_chat.repositories.ChatContactsRepository
import com.gigforce.modules.feature_chat.repositories.ChatProfileFirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MediaDocsAndAudioViewModel @Inject constructor(
    private val chatContactsRepository: ChatContactsRepository,
    private val chatGroupRepository: ChatGroupRepository,
    private val firebaseStorage: FirebaseStorage,
    private val chatProfileFirebaseRepository: ChatProfileFirebaseRepository,
    private val chatFileManager : ChatFileManager
)  : ViewModel() {

    //private lateinit var groupId: String

    private var groupDetails: ChatGroup? = null
    private var groupDetailsListener: ListenerRegistration? = null

    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    /**
     * -----------------------------------
     * Getting group details
     * -----------------------------------
     */

    private val _groupInfo: MutableLiveData<ChatGroup> = MutableLiveData()
    val groupInfo: LiveData<ChatGroup> = _groupInfo

    //Data
    private var mediaRaw: List<GroupMedia>? = null
    private var mediaListShownOnView: MutableList<ChatMediaViewModels> = mutableListOf()
    var currentFilterString: String = "Media"

    fun startWatchingGroupDetails(groupId: String) {

        if (groupDetailsListener != null) {
            Log.d(TAG, "already a listener attached,no-op")
            return
        }

        groupDetailsListener = chatGroupRepository.getGroupDetailsRef(groupId)
            .addSnapshotListener { data, error ->
                Log.d(TAG, "group details changed/subscribed, groupId - $groupId")

                error?.let {
                    CrashlyticsLogger.e(TAG, "In startWatchingGroupDetails()", it)
                }

                if (data != null) {
                    groupDetails = data.toObject(ChatGroup::class.java)!!.apply {
                        this.id = data.id
                    }

                    val isUserDeletedFromgroup =
                        groupDetails!!.deletedGroupMembers.find { it.uid == currentUser.uid } != null
                    val limitToTimeStamp = if (isUserDeletedFromgroup) {
                        groupDetails!!.deletedGroupMembers.find { it.uid == currentUser.uid }!!.deletedOn
                    } else
                        null

                    if(groupDetails != null){
                        _groupInfo.value = groupDetails
                        mediaRaw = groupDetails!!.groupMedia
                        processMediaAndEmit(mediaRaw!!)

                    }
                }
            }
    }

    private fun processMediaAndEmit(mediaRaw: List<GroupMedia>) {
        val imageAndVideoList = mediaRaw.filter {
            it.attachmentType == ChatConstants.ATTACHMENT_TYPE_IMAGE
        } + mediaRaw.filter {
            it.attachmentType == ChatConstants.ATTACHMENT_TYPE_VIDEO
        }

        val documentList = mediaRaw.filter {
            it.attachmentType == ChatConstants.ATTACHMENT_TYPE_DOCUMENT
        }

        val audioList = mediaRaw.filter {
            it.attachmentType == ChatConstants.ATTACHMENT_TYPE_AUDIO
        }


        Log.d(TAG, "image list: $imageAndVideoList , doc list : $documentList , audioList: $audioList")

//        imageAndVideoList.groupBy {
//            it.timestamp
//        }

    }

    companion object {
        const val TAG: String = "MediaDocsAndAudioVM"
    }

}