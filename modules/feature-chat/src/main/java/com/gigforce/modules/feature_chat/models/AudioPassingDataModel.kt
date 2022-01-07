package com.gigforce.modules.feature_chat.models

import android.net.Uri

data class AudioPassingDataModel(
    var playPause: Boolean? = false,
    var isAudioPlaying: Boolean? = false,
    var currentlyPlayingAudioId: String? = null,
    var playingAudioUri: Uri? = null
) {
}