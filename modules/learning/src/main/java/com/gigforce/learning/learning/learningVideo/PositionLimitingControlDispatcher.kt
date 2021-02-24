package com.gigforce.learning.learning.learningVideo

import com.google.android.exoplayer2.DefaultControlDispatcher
import com.google.android.exoplayer2.Player

class PositionLimitingControlDispatcher : DefaultControlDispatcher() {
    private var maxPlayedPositionMs: Long = 0

    // Needs to be called from application code whenever a new playback starts.
    fun reset() {
        maxPlayedPositionMs = 0
    }

    // Note: This implementation assumes single window content. You might need to do
    // something more complicated, depending on your use case.
    override fun dispatchSeekTo(player: Player, windowIndex: Int, positionMs: Long): Boolean {
        maxPlayedPositionMs = Math.max(maxPlayedPositionMs, player.currentPosition)
        player.seekTo(windowIndex, Math.min(positionMs, maxPlayedPositionMs))
        return true
    }
}