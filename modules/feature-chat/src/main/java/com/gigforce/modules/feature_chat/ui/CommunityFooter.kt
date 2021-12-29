package com.gigforce.modules.feature_chat.ui

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.*
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.aghajari.emojiview.emoji.Emoji
import com.aghajari.emojiview.listener.OnEmojiActions
import com.aghajari.emojiview.listener.PopupListener
import com.aghajari.emojiview.view.AXEmojiView
import com.aghajari.emojiview.view.AXSingleEmojiView
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.chat.models.MentionUser
import com.gigforce.common_ui.core.ChatConstants
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.databinding.CommunityFooterLayoutBinding
import com.gigforce.modules.feature_chat.models.GroupChatMember
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.linkedin.android.spyglass.suggestions.SuggestionsResult
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsResultListener
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsVisibilityManager
import com.linkedin.android.spyglass.tokenization.QueryToken
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizer
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizerConfig
import com.linkedin.android.spyglass.tokenization.interfaces.QueryTokenReceiver
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType
import android.graphics.drawable.Drawable

import com.aghajari.emojiview.listener.SimplePopupAdapter
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener


@AndroidEntryPoint
class CommunityFooter(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs),
    QueryTokenReceiver,
    SuggestionsVisibilityManager,
    SuggestionsResultListener
    {

    companion object {
        private const val SUGGESTION_BUCKET = "names-suggestions"
        private const val TAG = "CommunityFooter"

        private val tokenizerConfig = WordTokenizerConfig.Builder()
            .setWordBreakChars(", ")
            .setExplicitChars("@")
//                .setExplicitChars("")
            .setMaxNumKeywords(2)
            .setThreshold(1)
            .build()
    }
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


        var viewBinding: CommunityFooterLayoutBinding
        private lateinit var CommunityFooter: CommunityFooter

        private lateinit var mentionAdapter: MemberMentionAdapter

        private var replyMessage : ChatMessage? = null

        private lateinit var viewModel: GroupChatViewModel

        private lateinit var attachmentOptionList: List<AttachmentOption>
        private lateinit var attachmentOptionsListener: AttachmentOptionsListener

        private var animBlink: Animation? = null
        private var animJump:Animation? = null
        private var animJumpFast:Animation? = null

        private var isDeleting = false
        private var stopTrackingAction = false
        lateinit var handlerS: Handler

        private var audioTotalTime: Long = 0
        private var timerTask: TimerTask? = null
        private var audioTimer: Timer? = null
        private var timeFormatter: SimpleDateFormat? = null


        private var lastX = 0f
        private  var lastY:Float = 0f
        private var firstX = 0f
        private  var firstY:Float = 0f

        private val directionOffset = 0f
        private  var cancelOffset:Float = 0f
        private  var lockOffset:Float = 0f
        private var dp = 0f
        private var isLocked = false

        private var userBehaviour = UserBehaviour.NONE
        private var recordingListener: RecordingListener? = null

        var isLayoutDirectionRightToLeft = false

        var screenWidth = 0
        var screenHeight:Int = 0

        private var layoutAttachments: MutableList<LinearLayout> = arrayListOf()

        private val showCameraIcon = true
        private  var showAttachmentIcon:Boolean = true
        private  var showEmojiIcon:Boolean = true
        private val removeAttachmentOptionAnimation = false

        //private lateinit var emojiView: AXEmojiView

        init {
            viewBinding = CommunityFooterLayoutBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )

            setUpViews()
        }

        private fun setUpViews() {
            timeFormatter = SimpleDateFormat("m:ss", Locale.getDefault())
            val displayMetrics = context.resources.displayMetrics
            screenHeight = displayMetrics.heightPixels
            screenWidth = displayMetrics.widthPixels
            isLayoutDirectionRightToLeft = context.resources.getBoolean(R.bool.is_right_to_left)
            mentionAdapter = MemberMentionAdapter(emptyList())

            Handler(context.mainLooper).also { handlerS = it }

            dp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1f,
                context.resources.displayMetrics
            )
            animBlink = AnimationUtils.loadAnimation(
                context,
                R.anim.blink
            )
            animJump = AnimationUtils.loadAnimation(
                context,
                R.anim.jump
            )
            animJumpFast = AnimationUtils.loadAnimation(
                context,
                R.anim.jump_fast
            )
            //setupEmojiLayout()
            setupRecording()
            setupAttachmentOptions()


        }

         fun setupEmojiLayout(emojiView: AXEmojiView) = viewBinding.apply {
             emojiView.setEditText(edt);
            emojiLayout.initPopupView(emojiView)
//             editTextMessage.setOnClickListener {
//                 if (emojiLayout.isShowing()) {
//                     emojiLayout.dismiss()
//                 }
//             }
             val imm by lazy { context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
             val windowHeightMethod = InputMethodManager::class.java.getMethod("getInputMethodWindowVisibleHeight")
             val height = windowHeightMethod.invoke(imm) as Int

             var isEmojiShowing = false
//             imageViewEmoji.setOnClickListener{
//                 if (emojiLayout.isShowing) {
//
//                     Log.d("emojilayout", "was already showing ${emojiLayout.isShowing} , iskeybordopen ${emojiLayout.isKeyboardOpen}")
//
//                     isEmojiShowing = false
//                     openSoftKeyboard(it)
////                     if (!emojiLayout.isKeyboardOpen){
////                         Log.d("emojilayout", "keyboard was hidden")
////
////                     }
//                     emojiLayout.dismiss()
//                     //emojiLayout.gone()
//
//                 } else{
//
//                     Log.d("emojilayout", "showing layout, ${emojiLayout.isShowing} , iskeybordopen ${emojiLayout.isKeyboardOpen}")
//                     isEmojiShowing = true
////                     if (emojiLayout.isKeyboardOpen){
////                         Log.d("emojilayout", "keyboard was showing")
////
////                     }
//                     hideKeyboard(it)
//                     emojiLayout.show()
//                     //emojiLayout.visible()
//                 }
//             }

             viewBinding.root.viewTreeObserver.addOnGlobalLayoutListener {
                 val heightDiff = viewBinding.root.rootView.height - viewBinding.root.height
                 if (heightDiff > 100) { // Value should be less than keyboard's height
                     Log.e("MyActivity", "keyboard opened")
                 } else {
                     Log.e("MyActivity", "keyboard closed")
                 }
             }


             emojiLayout.setPopupListener(object : SimplePopupAdapter() {
                 override fun onShow() {
                     imageViewEmoji.setImageDrawable(resources.getDrawable(R.drawable.ic_attachment_icon))
                 }

                 override fun onDismiss() {
                     imageViewEmoji.setImageDrawable(resources.getDrawable(R.drawable.ic_emoji_icon))
                 }
             })

             var isEmojiLayoutShowing = false

             emojiView.onEmojiActionsListener = object :  OnEmojiActions {
                 override fun onClick(
                     view: View?,
                     emoji: Emoji?,
                     fromRecent: Boolean,
                     fromVariant: Boolean
                 ) {
                     val textH = editTextMessage.text.toString() + emoji
                     editTextMessage.setSelection(editTextMessage.length())
                     editTextMessage.setText(textH)
                 }

                 override fun onLongClick(
                     view: View?,
                     emoji: Emoji?,
                     fromRecent: Boolean,
                     fromVariant: Boolean
                 ): Boolean {
                     return true
                 }
             }

//             emojiLayout.setPopupListener(object : PopupListener {
//                 override fun onDismiss() {
//                     Log.d("emojiLayout", "dismiss")
//                     imageViewEmoji.setImageDrawable(resources.getDrawable(R.drawable.ic_emoji_icon))
////                     imageViewEmoji.requestFocus()
//                 }
//
//                 override fun onShow() {
//                     Log.d("emojiLayout", "show")
//                     imageViewEmoji.setImageDrawable(resources.getDrawable(R.drawable.ic_attachment_icon))
////                     imageViewEmoji.requestFocus()
//                 }
//
//                 override fun onKeyboardOpened(height: Int) {
//                     Log.d("emojiLayout", "keyboard opened")
//                     //emojiLayout.dismiss()
//                 }
//
//                 override fun onKeyboardClosed() {
//                     Log.d("emojiLayout", "keyboard closed")
//                 }
//
//                 override fun onViewHeightChanged(height: Int) {
//                     Log.d("emojiLayout", "height: $height")
//                 }
//
//
//             })
//            imageViewEmoji.setOnClickListener {
//                val handler = Handler()
//                handler.postDelayed({
////                    emojiLayout.toggle()
//                    if (emojiLayout.isShowing){
//                        //emojiLayout.toggle()
//                        emojiLayout.dismiss()
//                        openSoftKeyboard(editTextMessage)
//                        editTextMessage.requestFocus()
//                        imageViewEmoji.setImageDrawable(resources.getDrawable(R.drawable.ic_emoji_icon))
//                    } else {
//                        //emojiLayout.toggle()
//                        emojiLayout.show()
//                        hideKeyboard(editTextMessage)
//                        editTextMessage.requestFocus()
//                        imageViewEmoji.setImageDrawable(resources.getDrawable(R.drawable.ic_attachment_icon))
//                    }
//                }, 2000) //5 seconds
//            }
        }

        fun hideKeyboard(view: View) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInputFromWindow(
                view.applicationWindowToken,
                InputMethodManager.HIDE_IMPLICIT_ONLY,
                0
            )
        }

//        fun openSoftKeyboard(view: View) {
//            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            inputMethodManager.toggleSoftInputFromWindow(
//                view.applicationWindowToken,
//                InputMethod.SHOW_FORCED,
//                0
//            )
//        }


        fun setGroupViewModel(viewModel: GroupChatViewModel) {
            this.viewModel = viewModel
        }

        fun disableInput(
            message : String
        ) = viewBinding.apply{
            //replyToMessageLayout.gone()
            replyBlockedLayout.visible()
            replyBlockedLayout.text = message
        }

        fun enableInput() = viewBinding.apply{
            replyBlockedLayout.gone()
            //replyToMessageLayout.visible()
            replyBlockedLayout.text = ""
        }

        fun enableUserSuggestions() = viewBinding.apply {
            editTextMessage.tokenizer = WordTokenizer(tokenizerConfig)
            editTextMessage.setQueryTokenReceiver(this@CommunityFooter)
            editTextMessage.setSuggestionsVisibilityManager(this@CommunityFooter)
        }

        private fun setupRecording() = viewBinding.apply{
//            imageViewSend.setOnClickListener {
//                //should not happen anything
//                Toast.makeText(context, "Hold to record", Toast.LENGTH_SHORT).show()
//            }

            imageViewSend.animate().scaleX(0f).scaleY(0f).setDuration(100)
                .setInterpolator(LinearInterpolator()).start()
            editTextMessage.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (s.toString().trim { it <= ' ' }.isEmpty()) {
                        if (imageViewSend.getVisibility() != View.GONE) {
                            imageViewSend.setVisibility(View.GONE)
                            imageViewSend.animate().scaleX(0f).scaleY(0f).setDuration(100)
                                .setInterpolator(LinearInterpolator()).start()
                        }
                        if (showCameraIcon) {
                            if (imageViewCamera.getVisibility() != View.VISIBLE && !isLocked) {
                                imageViewCamera.setVisibility(View.VISIBLE)
                                imageViewCamera.animate().scaleX(1f).scaleY(1f).setDuration(100)
                                    .setInterpolator(LinearInterpolator()).start()
                            }
                        }
                    } else {
                        if (imageViewSend.getVisibility() != View.VISIBLE && !isLocked) {
                            imageViewSend.setVisibility(View.VISIBLE)
                            imageViewSend.animate().scaleX(1f).scaleY(1f).setDuration(100)
                                .setInterpolator(LinearInterpolator()).start()
                        }
                        if (showCameraIcon) {
                            if (imageViewCamera.getVisibility() != View.GONE) {
                                imageViewCamera.setVisibility(View.GONE)
                                imageViewCamera.animate().scaleX(0f).scaleY(0f).setDuration(100)
                                    .setInterpolator(LinearInterpolator()).start()
                            }
                        }
                    }
                }
            })

            imageViewAudio!!.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
                if (isDeleting) {
                    return@OnTouchListener true
                }
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    cancelOffset = (screenWidth / 2.8).toFloat()
                    lockOffset = (screenWidth / 2.5).toFloat()
                    if (firstX == 0f) {
                        firstX = motionEvent.rawX
                    }
                    if (firstY == 0f) {
                        firstY = motionEvent.rawY
                    }
                    startRecord()
                } else if (motionEvent.action == MotionEvent.ACTION_UP
                    || motionEvent.action == MotionEvent.ACTION_CANCEL
                ) {
                    if (motionEvent.action == MotionEvent.ACTION_UP) {
                        stopRecording(RecordingBehaviour.RELEASED)
                    }
                } else if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                    if (stopTrackingAction) {
                        return@OnTouchListener true
                    }
                    var direction = UserBehaviour.NONE
                    val motionX = Math.abs(firstX - motionEvent.rawX)
                    val motionY: Float = Math.abs(firstY - motionEvent.rawY)
                    if (if (isLayoutDirectionRightToLeft) motionX > directionOffset && lastX > firstX && lastY > firstY else motionX > directionOffset && lastX < firstX && lastY < firstY) {
                        if (if (isLayoutDirectionRightToLeft) motionX > motionY && lastX > firstX else motionX > motionY && lastX < firstX) {
                            direction = UserBehaviour.CANCELING
                        } else if (motionY > motionX && lastY < firstY) {
                            direction = UserBehaviour.LOCKING
                        }
                    } else if (if (isLayoutDirectionRightToLeft) motionX > motionY && motionX > directionOffset && lastX > firstX else motionX > motionY && motionX > directionOffset && lastX < firstX) {
                        direction = UserBehaviour.CANCELING
                    } else if (motionY > motionX && motionY > directionOffset && lastY < firstY) {
                        direction = UserBehaviour.LOCKING
                    }
                    if (direction == UserBehaviour.CANCELING) {
                        if (userBehaviour == UserBehaviour.NONE || motionEvent.rawY + imageViewAudio!!.width / 2 > firstY) {
                            userBehaviour = UserBehaviour.CANCELING
                        }
                        if (userBehaviour == UserBehaviour.CANCELING) {
                            translateX(-(firstX - motionEvent.rawX))
                        }
                    } else if (direction == UserBehaviour.LOCKING) {
                        if (userBehaviour == UserBehaviour.NONE || motionEvent.rawX + imageViewAudio!!.width / 2 > firstX) {
                            userBehaviour = UserBehaviour.LOCKING
                        }
                        if (userBehaviour == UserBehaviour.LOCKING) {
                            translateY(-(firstY - motionEvent.rawY))
                        }
                    }
                    lastX = motionEvent.rawX
                    lastY = motionEvent.rawY
                }
                view.onTouchEvent(motionEvent)
                true
            })
            imageViewStop.setOnClickListener(View.OnClickListener {
                isLocked = false
                stopRecording(RecordingBehaviour.LOCK_DONE)
            })

        }

        fun setAttachmentOptions(
            attachmentOptionList: List<AttachmentOption>?,
            attachmentOptionsListener: AttachmentOptionsListener?
        ) = viewBinding.apply {
            if (attachmentOptionList != null) {
                this@CommunityFooter.attachmentOptionList = attachmentOptionList
            }
            this@CommunityFooter.attachmentOptionsListener = attachmentOptionsListener!!
            if (this@CommunityFooter.attachmentOptionList != null && !this@CommunityFooter.attachmentOptionList!!.isEmpty()) {
                layoutAttachmentOptions.removeAllViews()
                var count = 0
                var linearLayoutMain: LinearLayout? = null
    //            layoutAttachments = ArrayList()
                for (attachmentOption in this@CommunityFooter.attachmentOptionList) {
                    if (count == 6) {
                        break
                    }
                    if (count == 0 || count == 3) {
                        linearLayoutMain = LinearLayout(context)
                        linearLayoutMain.layoutParams =
                            LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        linearLayoutMain.orientation = LinearLayout.HORIZONTAL
                        linearLayoutMain.gravity = Gravity.CENTER
                        layoutAttachmentOptions.addView(linearLayoutMain)
                    }
                    val linearLayout = LinearLayout(context)
                    linearLayout.layoutParams = LinearLayout.LayoutParams(
                        (dp * 100).toInt(),
                        (dp * 100).toInt()
                    )
                    linearLayout.setPadding(
                        (dp * 4).toInt(), (dp * 4).toInt(), (dp * 4).toInt(),
                        (dp * 0).toInt()
                    )
                    linearLayout.orientation = LinearLayout.VERTICAL
                    linearLayout.gravity = Gravity.CENTER
                    layoutAttachments.add(linearLayout)
                    val imageView = ImageView(context)
                    imageView.layoutParams = LinearLayout.LayoutParams(
                        (dp * 76).toInt(),
                        (dp * 76).toInt()
                    )
                    imageView.setImageResource(attachmentOption.resourceImage)
                    val textView = TextView(context)
                    TextViewCompat.setTextAppearance(textView, R.style.TextAttachmentOptions)
                    textView.layoutParams =
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    textView.setPadding(
                        (dp * 4).toInt(), (dp * 0).toInt(), (dp * 4).toInt(),
                        (dp * 0).toInt()
                    )
                    textView.maxLines = 1
                    textView.setText(attachmentOption.title)
                    linearLayout.addView(imageView)
                    linearLayout.addView(textView)
                    val outValue = TypedValue()
                    context.theme.resolveAttribute(
                        android.R.attr.selectableItemBackground,
                        outValue,
                        true
                    )
                    linearLayout.setBackgroundResource(outValue.resourceId)
                    linearLayoutMain?.addView(linearLayout)
                    linearLayout.setOnClickListener {
                        hideAttachmentOptionView()
                        this@CommunityFooter.attachmentOptionsListener.onClick(attachmentOption)
                    }
                    count++
                }
            }
    }

        fun hideAttachmentOptionView() {
            if (viewBinding.layoutAttachment!!.visibility == View.VISIBLE) {
                viewBinding.imageViewAttachment.performClick()
            }
        }


        private fun setupAttachmentOptions() = viewBinding.apply{
            imageViewAttachment.setOnClickListener(View.OnClickListener {
                if (layoutAttachment!!.visibility == View.VISIBLE) {
                    val x =
                        if (isLayoutDirectionRightToLeft) (dp * (18 + 40 + 4 + 56)).toInt() else (screenWidth - dp * (18 + 40 + 4 + 56)).toInt()
                    val y = (dp * 220).toInt()
                    val startRadius = 0
                    val endRadius =
                        Math.hypot((screenWidth - dp * (8 + 8)).toDouble(), (dp * 220).toDouble())
                            .toInt()
                    val anim = ViewAnimationUtils.createCircularReveal(
                        layoutAttachment,
                        x,
                        y,
                        endRadius.toFloat(),
                        startRadius.toFloat()
                    )
                    anim.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animator: Animator) {}
                        override fun onAnimationEnd(animator: Animator) {
                            layoutAttachment!!.visibility = View.GONE
                        }

                        override fun onAnimationCancel(animator: Animator) {}
                        override fun onAnimationRepeat(animator: Animator) {}
                    })
                    anim.start()
                } else {
                    if (!removeAttachmentOptionAnimation) {
                        var count = 0
                        if (layoutAttachments != null && !layoutAttachments!!.isEmpty()) {
                            var arr = intArrayOf(5, 4, 2, 3, 1, 0)
                            if (isLayoutDirectionRightToLeft) {
                                arr = intArrayOf(3, 4, 0, 5, 1, 2)
                            }
                            for (i in layoutAttachments!!.indices) {
                                if (arr[i] < layoutAttachments!!.size) {
                                    val layout = layoutAttachments!![arr[i]]
                                    layout.scaleX = 0.4f
                                    layout.alpha = 0f
                                    layout.scaleY = 0.4f
                                    layout.translationY = dp * 48 * 2
                                    layout.visibility = View.INVISIBLE
                                    layout.animate().scaleX(1f).scaleY(1f).alpha(1f).translationY(0f)
                                        .setStartDelay((count * 25 + 50).toLong()).setDuration(300)
                                        .setInterpolator(OvershootInterpolator()).start()
                                    layout.visibility = View.VISIBLE
                                    count++
                                }
                            }
                        }
                    }
                    val x =
                        if (isLayoutDirectionRightToLeft) (dp * (18 + 40 + 4 + 56)).toInt() else (screenWidth - dp * (18 + 40 + 4 + 56)).toInt()
                    val y = (dp * 220).toInt()
                    val startRadius = 0
                    val endRadius =
                        Math.hypot((screenWidth - dp * (8 + 8)).toDouble(), (dp * 220).toDouble())
                            .toInt()
                    val anim = ViewAnimationUtils.createCircularReveal(
                        layoutAttachment,
                        x,
                        y,
                        startRadius.toFloat(),
                        endRadius.toFloat()
                    )
                    anim.duration = 500
                    layoutAttachment!!.visibility = View.VISIBLE
                    anim.start()
                }
            })
        }


        private fun translateY(y: Float) = viewBinding.apply {
            if (y < -lockOffset) {
                locked()
                imageViewAudio!!.translationY = 0f
                return@apply
            }
            if (layoutLock.getVisibility() != View.VISIBLE) {
                layoutLock.setVisibility(View.VISIBLE)
            }
            imageViewAudio!!.translationY = y
            layoutLock.setTranslationY(y / 2)
            imageViewAudio!!.translationX = 0f
        }

        private fun translateX(x: Float) = viewBinding.apply {
            if (if (isLayoutDirectionRightToLeft) x > cancelOffset else x < -cancelOffset) {
                canceled()
                imageViewAudio!!.translationX = 0f
                layoutSlideCancel!!.translationX = 0f
                return@apply
            }
            imageViewAudio!!.translationX = x
            layoutSlideCancel!!.translationX = x
            layoutLock.setTranslationY(0f)
            imageViewAudio!!.translationY = 0f
            if (Math.abs(x) < imageViewMic.getWidth() / 2) {
                if (layoutLock.getVisibility() != View.VISIBLE) {
                    layoutLock.setVisibility(View.VISIBLE)
                }
            } else {
                if (layoutLock.getVisibility() != View.GONE) {
                    layoutLock.setVisibility(View.GONE)
                }
            }
        }

        private fun locked() = viewBinding.apply  {
            stopTrackingAction = true
            stopRecording(RecordingBehaviour.LOCKED)
            isLocked = true
        }

        fun setRecordingListener(recordingListener: CommunityFooter.RecordingListener?) {
            if (recordingListener != null) {
                this.recordingListener = recordingListener
            }
        }


        private fun canceled()  = viewBinding.apply {
            stopTrackingAction = true
            stopRecording(RecordingBehaviour.CANCELED)
        }

         fun stopRecording(recordingBehaviour: RecordingBehaviour)  = viewBinding.apply {
            stopTrackingAction = true
            firstX = 0f
            firstY = 0f
            lastX = 0f
            lastY = 0f
            userBehaviour = UserBehaviour.NONE
            imageViewAudio!!.animate().scaleX(1f).scaleY(1f).translationX(0f).translationY(0f)
                .setDuration(100).setInterpolator(LinearInterpolator()).start()
            layoutSlideCancel!!.translationX = 0f
            layoutSlideCancel!!.visibility = View.GONE
            layoutLock.setVisibility(View.GONE)
            layoutLock.setTranslationY(0f)
            imageViewLockArrow.clearAnimation()
            imageViewLock.clearAnimation()
            if (isLocked) {
                return@apply
            }
            if (recordingBehaviour == RecordingBehaviour.LOCKED) {
                imageViewStop.setVisibility(View.VISIBLE)
                if (recordingListener != null) recordingListener!!.onRecordingLocked()
            } else if (recordingBehaviour == RecordingBehaviour.CANCELED) {
                textViewTime!!.clearAnimation()
                textViewTime!!.visibility = View.INVISIBLE
                imageViewMic.setVisibility(View.INVISIBLE)
                imageViewStop.setVisibility(View.GONE)
                layoutEffect2.setVisibility(View.GONE)
                layoutEffect1.setVisibility(View.GONE)
                timerTask!!.cancel()
                delete()
                if (recordingListener != null) recordingListener!!.onRecordingCanceled()
            } else if (recordingBehaviour == RecordingBehaviour.RELEASED || recordingBehaviour == RecordingBehaviour.LOCK_DONE) {
                textViewTime!!.clearAnimation()
                textViewTime!!.visibility = View.INVISIBLE
                imageViewMic.setVisibility(View.INVISIBLE)
                editTextMessage!!.visibility = View.VISIBLE
                if (showAttachmentIcon) {
                    imageViewAttachment.setVisibility(View.VISIBLE)
                }
                if (showCameraIcon) {
                    imageViewCamera.setVisibility(View.VISIBLE)
                }
                if (showEmojiIcon) {
                    imageViewEmoji.setVisibility(View.VISIBLE)
                }
                imageViewStop.setVisibility(View.GONE)
                editTextMessage!!.requestFocus()
                layoutEffect2.setVisibility(View.GONE)
                layoutEffect1.setVisibility(View.GONE)
                timerTask!!.cancel()
                if (recordingListener != null) recordingListener!!.onRecordingCompleted()
            }
        }

        private fun startRecord()  = viewBinding.apply {
            if (recordingListener != null) recordingListener!!.onRecordingStarted()
            //hideAttachmentOptionView()
            stopTrackingAction = false
            editTextMessage!!.visibility = View.INVISIBLE
            imageViewAttachment.setVisibility(View.INVISIBLE)
            imageViewCamera.setVisibility(View.INVISIBLE)
            imageViewEmoji.setVisibility(View.INVISIBLE)
            imageViewAudio!!.animate().scaleXBy(1f).scaleYBy(1f).setDuration(200)
                .setInterpolator(OvershootInterpolator()).start()
            textViewTime!!.visibility = View.VISIBLE
            layoutLock.setVisibility(View.VISIBLE)
            layoutSlideCancel!!.visibility = View.VISIBLE
            imageViewMic.setVisibility(View.VISIBLE)
            layoutEffect2.setVisibility(View.VISIBLE)
            layoutEffect1.setVisibility(View.VISIBLE)
            textViewTime!!.startAnimation(animBlink)
            imageViewLockArrow.clearAnimation()
            imageViewLock.clearAnimation()
            imageViewLockArrow.startAnimation(animJumpFast)
            imageViewLock.startAnimation(animJump)
            if (audioTimer == null) {
                audioTimer = Timer()
                timeFormatter!!.timeZone = TimeZone.getTimeZone("UTC")
            }
            audioTotalTime = 0
            timerTask = object : TimerTask() {
                override fun run() {
                    handler?.post {
                        textViewTime.setText(timeFormatter!!.format(Date(audioTotalTime * 1000)))
                        audioTotalTime++
                    }
                }
            }

            audioTimer!!.schedule(timerTask, 0, 1000)
        }

        private fun delete()  = viewBinding.apply  {
            imageViewMic.setVisibility(View.VISIBLE)
            imageViewMic.setRotation(0f)
            isDeleting = true
            imageViewAudio!!.isEnabled = false
            handler!!.postDelayed({
                isDeleting = false
                imageViewAudio!!.isEnabled = true
                if (showAttachmentIcon) {
                    imageViewAttachment.setVisibility(View.VISIBLE)
                }
                if (showCameraIcon) {
                    imageViewCamera.setVisibility(View.VISIBLE)
                }
                if (showEmojiIcon) {
                    imageViewEmoji.setVisibility(View.VISIBLE)
                }
            }, 1250)
            imageViewMic.animate().translationY(-dp * 150).rotation(180f).scaleXBy(0.6f).scaleYBy(0.6f)
                .setDuration(500).setInterpolator(
                    DecelerateInterpolator()
                ).setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        var displacement = 0f
                        displacement = if (isLayoutDirectionRightToLeft) {
                            dp * 40
                        } else {
                            -dp * 40
                        }
                        dustin.setTranslationX(displacement)
                        dustinCover.setTranslationX(displacement)
                        dustinCover.animate().translationX(0f).rotation(-120f).setDuration(350)
                            .setInterpolator(
                                DecelerateInterpolator()
                            ).start()
                        dustin.animate().translationX(0f).setDuration(350).setInterpolator(
                            DecelerateInterpolator()
                        ).setListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                                dustin.setVisibility(View.VISIBLE)
                                dustinCover.setVisibility(View.VISIBLE)
                            }

                            override fun onAnimationEnd(animation: Animator) {}
                            override fun onAnimationCancel(animation: Animator) {}
                            override fun onAnimationRepeat(animation: Animator) {}
                        }).start()
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        imageViewMic.animate().translationY(0f).scaleX(1f).scaleY(1f).setDuration(350)
                            .setInterpolator(LinearInterpolator()).setListener(
                                object : Animator.AnimatorListener {
                                    override fun onAnimationStart(animation: Animator) {}
                                    override fun onAnimationEnd(animation: Animator) {
                                        imageViewMic.setVisibility(View.INVISIBLE)
                                        imageViewMic.setRotation(0f)
                                        var displacement = 0f
                                        displacement = if (isLayoutDirectionRightToLeft) {
                                            dp * 40
                                        } else {
                                            -dp * 40
                                        }
                                        dustinCover.animate().rotation(0f).setDuration(150).setStartDelay(50)
                                            .start()
                                        dustin.animate().translationX(displacement).setDuration(200)
                                            .setStartDelay(250).setInterpolator(
                                                DecelerateInterpolator()
                                            ).start()
                                        dustinCover.animate().translationX(displacement).setDuration(200)
                                            .setStartDelay(250).setInterpolator(
                                                DecelerateInterpolator()
                                            ).setListener(object : Animator.AnimatorListener {
                                                override fun onAnimationStart(animation: Animator) {}
                                                override fun onAnimationEnd(animation: Animator) {
                                                    editTextMessage!!.visibility = View.VISIBLE
                                                    editTextMessage!!.requestFocus()
                                                }

                                                override fun onAnimationCancel(animation: Animator) {}
                                                override fun onAnimationRepeat(animation: Animator) {}
                                            }).start()
                                    }

                                    override fun onAnimationCancel(animation: Animator) {}
                                    override fun onAnimationRepeat(animation: Animator) {}
                                }
                            ).start()
                    }

                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                }).start()
        }

        private fun showErrorLog(s: String) {
            Log.e(TAG, s)
        }

        fun getMentionedPeopleInText(): List<MentionUser> {
            val text = viewBinding.editTextMessage.mentionsText
            val mentionedSpans = text.mentionSpans

            if (mentionedSpans.isEmpty())
                return emptyList()

            val personMentions: MutableList<MentionUser> = mutableListOf()
            mentionedSpans.forEach { span ->

                val start = text.getSpanStart(span)
                val end = text.getSpanEnd(span)
                val mentionedPerson = span.mention as GroupChatMember

                personMentions.add(
                    MentionUser(
                        startFrom = start,
                        endTo = end,
                        userMentionedUid = mentionedPerson.uid,
                        profileName = mentionedPerson.name,
                        profilePicture = mentionedPerson.profilePicture
                    )
                )
            }

            return personMentions
        }

        fun isTypingEnabled() : Boolean{
            return viewBinding.replyBlockedLayout.isVisible
        }

    override fun onQueryReceived(queryToken: QueryToken): MutableList<String> {
        val buckets = Collections.singletonList(SUGGESTION_BUCKET)
        val nameSuggestions = viewModel.getGroupMembersNameSuggestions(queryToken.keywords)
        val result = SuggestionsResult(queryToken, nameSuggestions)
        onReceiveSuggestionsResult(result, SUGGESTION_BUCKET)
        return buckets
    }

    override fun displaySuggestions(display: Boolean) {
        viewBinding.mentionSuggestionRv.isVisible = display
    }

    override fun isDisplayingSuggestions(): Boolean {
        return viewBinding.mentionSuggestionRv.isVisible
    }

    override fun onReceiveSuggestionsResult(result: SuggestionsResult, bucket: String) {
        val suggestions = result.suggestions as List<GroupChatMember>
        mentionAdapter = MemberMentionAdapter(suggestions)
        viewBinding.mentionSuggestionRv.swapAdapter(mentionAdapter, true)
        displaySuggestions(suggestions.isNotEmpty())
    }

        // --------------------------------------------------
        // MemberMentionAdapter Class
        // --------------------------------------------------
        private class SuggestionsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var name: TextView = itemView.findViewById(R.id.person_name)
            var picture: GigforceImageView = itemView.findViewById(R.id.person_image)
        }

        private inner class MemberMentionAdapter(
            private val suggestions: List<GroupChatMember>
        ) : RecyclerView.Adapter<SuggestionsItemViewHolder>() {

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SuggestionsItemViewHolder {
                val v: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.recycler_item_mention_suggestion, viewGroup, false)
                return SuggestionsItemViewHolder(v)
            }

            override fun onBindViewHolder(viewHolder: SuggestionsItemViewHolder, i: Int) {
                val person = suggestions[i]
                viewHolder.name.text = person.name

                if (person.profilePicture.isNotBlank())
                    viewHolder.picture.loadImageIfUrlElseTryFirebaseStorage(person.profilePicture, R.drawable.ic_user_2, R.drawable.ic_user_2)
                else {
                    viewHolder.picture.loadImage(R.drawable.ic_user_2)
                }

                viewHolder.itemView.setOnClickListener {
                    viewBinding.editTextMessage.insertMention(person)
                    viewBinding.mentionSuggestionRv.swapAdapter(MemberMentionAdapter(emptyList()), true)
                    displaySuggestions(false)
                    viewBinding.editTextMessage.requestFocus()
                }
            }

            override fun getItemCount(): Int {
                return suggestions.size
            }
        }

        fun openReplyUi(
            chatMessage: ChatMessage
        ) = viewBinding.apply {
            this@CommunityFooter.replyMessage = chatMessage
            replyToMessageLayout.visible()
            replyToMessageLayout.removeAllViews()

            val replyView = LayoutInflater.from(context).inflate(
                R.layout.layout_reply_to_layout,
                null,
                false
            )
            replyToMessageLayout.addView(replyView)

            //Setting common vars and listeners
            val senderNameTV: TextView = replyView.findViewById(R.id.user_name_tv)
            val messageTV: TextView = replyView.findViewById(R.id.tv_msgValue)
            val closeBtn: ImageView = replyView.findViewById(R.id.close_btn)
            val messageImageIV: GigforceImageView = replyView.findViewById(R.id.message_image)

            closeBtn.setOnClickListener {
                closeReplyUi()
            }
            senderNameTV.text = chatMessage.senderInfo.name

            when (chatMessage.type) {
                ChatConstants.MESSAGE_TYPE_TEXT -> {
                    messageTV.text = chatMessage.content
                    messageImageIV.gone()
                }
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> {
                    messageTV.text = chatMessage.attachmentName
                    messageImageIV.visible()

                    if(chatMessage.thumbnailBitmap != null){
                        messageImageIV.loadImage(chatMessage.thumbnailBitmap!!,true)
                    } else if(chatMessage.thumbnail != null){
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.thumbnail!!)
                    }else if(chatMessage.attachmentPath != null){
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.attachmentPath!!)
                    } else {
                        //load default image
                    }
                }
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> {
                    messageTV.text = chatMessage.attachmentName
                    messageImageIV.visible()

                    if(chatMessage.thumbnailBitmap != null){
                        messageImageIV.loadImage(chatMessage.thumbnailBitmap!!,true)
                    } else if(chatMessage.thumbnail != null){
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.thumbnail!!)
                    }else {
                        //load default image
                    }
                }
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION -> {
                    messageTV.text = chatMessage.locationPhysicalAddress
                    messageImageIV.visible()

                    if(chatMessage.thumbnailBitmap != null){
                        messageImageIV.loadImage(chatMessage.thumbnailBitmap!!,true)
                    } else if(chatMessage.thumbnail != null){
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.thumbnail!!)
                    }else if(chatMessage.attachmentPath != null){
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.attachmentPath!!)
                    } else {
                        //load default image
                    }
                }
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> {
                    messageTV.text = chatMessage.attachmentName
                    messageImageIV.visible()
                    messageImageIV.loadImage(R.drawable.ic_document_background)
                }
                else -> {
                }
            }


        }

        fun closeReplyUi(){
            viewBinding.replyToMessageLayout.removeAllViews()
            viewBinding.replyToMessageLayout.gone()
            replyMessage = null
        }

        fun getReplyToMessage(): ChatMessage? {
            return replyMessage
        }

        fun openSoftKeyboard(view: View) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInputFromWindow(
                view.applicationWindowToken,
                InputMethod.SHOW_FORCED,
                0
            )
        }

    }