package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.common_ui.databinding.AppBarLayoutBinding
import com.gigforce.modules.feature_chat.databinding.CommunityFooterLayoutBinding
import com.linkedin.android.spyglass.suggestions.SuggestionsResult
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsResultListener
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsVisibilityManager
import com.linkedin.android.spyglass.tokenization.QueryToken
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizerConfig
import com.linkedin.android.spyglass.tokenization.interfaces.QueryTokenReceiver
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

enum class UserBehaviour {
    CANCELING, LOCKING, NONE
}

enum class RecordingBehaviour {
    CANCELED, LOCKED, LOCK_DONE, RELEASED
}

interface RecordingListener {
    fun onRecordingStarted()
    fun onRecordingLocked()
    fun onRecordingCompleted()
    fun onRecordingCanceled()
}


@AndroidEntryPoint
class CommunityFooter(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs)
    {

    companion object {
        private const val SUGGESTION_BUCKET = "names-suggestions"

        private val tokenizerConfig = WordTokenizerConfig.Builder()
            .setWordBreakChars(", ")
            .setExplicitChars("@")
//                .setExplicitChars("")
            .setMaxNumKeywords(2)
            .setThreshold(1)
            .build()
    }

        private lateinit var viewBinding: CommunityFooterLayoutBinding
        private lateinit var audioRecordView: AudioRecordView

        private val animBlink: Animation? = null
        private var animJump:Animation? = null
        private var animJumpFast:Animation? = null

        private val isDeleting = false
        private val stopTrackingAction = false
        //private val handler: Handler? = null

        private val audioTotalTime = 0
        private val timerTask: TimerTask? = null
        private val audioTimer: Timer? = null
        private val timeFormatter: SimpleDateFormat? = null


        private val lastX = 0f
        private  var lastY:Float = 0f
        private val firstX = 0f
        private  var firstY:Float = 0f

        private val directionOffset = 0f
        private  var cancelOffset:Float = 0f
        private  var lockOffset:Float = 0f
        private val dp = 0f
        private val isLocked = false

        private val userBehaviour = UserBehaviour.NONE
        private val recordingListener: RecordingListener? = null

        var isLayoutDirectionRightToLeft = false

        var screenWidth = 0
        var screenHeight:Int = 0

        private val showCameraIcon = true
        private  var showAttachmentIcon:Boolean = true
        private  var showEmojiIcon:Boolean = true
        private val removeAttachmentOptionAnimation = false

        init {
            viewBinding = CommunityFooterLayoutBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )

            setUpViews()
        }

//    override fun onQueryReceived(queryToken: QueryToken): MutableList<String> {
//
//    }
//
//    override fun displaySuggestions(display: Boolean) {
//
//    }
//
//    override fun isDisplayingSuggestions(): Boolean {
//
//    }
//
//    override fun onReceiveSuggestionsResult(result: SuggestionsResult, bucket: String) {
//
//    }
}