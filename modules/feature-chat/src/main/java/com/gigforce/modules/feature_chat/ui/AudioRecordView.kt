package com.gigforce.modules.feature_chat.ui

import android.animation.Animator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.gigforce.modules.feature_chat.R
import java.text.SimpleDateFormat
import java.util.*

class AudioRecordView {
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

    private val TAG = "AudioRecordView"

    private lateinit var viewContainer: LinearLayout
    private lateinit var layoutAttachmentOptions:LinearLayout
    private lateinit var imageViewAudio: View
    private  lateinit var imageViewLockArrow:View
    private lateinit var imageViewLock:View
    private lateinit var imageViewMic:View
    private lateinit var dustin:View
    private lateinit var dustin_cover:View
    private lateinit var imageViewStop:View
    private lateinit var imageViewSend:View
    private lateinit var layoutAttachment: View
    private lateinit var layoutDustin:View
    private lateinit var layoutMessage:View
    private lateinit var imageViewAttachment:View
    private lateinit var imageViewCamera:View
    private lateinit var imageViewEmoji:View
    private lateinit var layoutSlideCancel: View
    private lateinit var layoutLock:View
    private lateinit var layoutEffect1:View
    private lateinit var layoutEffect2:View
    private lateinit var editTextMessage: EditText
    private lateinit var timeText: TextView
    private lateinit var textViewSlide:TextView

    private lateinit var stop: ImageView
    private lateinit var audio:ImageView
    private lateinit var send:ImageView

    private lateinit var animBlink: Animation
    private lateinit var animJump:Animation
    private lateinit var animJumpFast:Animation

    private var isDeleting = false
    private var stopTrackingAction = false
    private lateinit var handler: Handler

    private var audioTotalTime = 0
    private lateinit var timerTask: TimerTask
    private lateinit var audioTimer: Timer
    private lateinit var timeFormatter: SimpleDateFormat

    private var lastX = 0f
    private  var lastY:kotlin.Float = 0f
    private var firstX = 0f
    private  var firstY:kotlin.Float = 0f

    private val directionOffset = 0f
    private  var cancelOffset:kotlin.Float = 0f
    private  var lockOffset:kotlin.Float = 0f
    private var dp = 0f
    private var isLocked = false

    private var userBehaviour = UserBehaviour.NONE
    private lateinit var recordingListener: RecordingListener

    var isLayoutDirectionRightToLeft = false

    var screenWidth = 0
    var screenHeight:Int = 0

//    private var attachmentOptionList: List<AttachmentOption>
//    private var attachmentOptionsListener: AttachmentOptionsListener

    private lateinit var layoutAttachments: MutableList<LinearLayout>

    private lateinit var context: Context

    private var showCameraIcon =
        true
    private  var showAttachmentIcon:Boolean = true
    private  var showEmojiIcon:Boolean = true
    private var removeAttachmentOptionAnimation = false

    fun initView(view: ViewGroup?) {
        if (view == null) {
            showErrorLog("initView ViewGroup can't be NULL")
            return
        }
        context = view.context
        view.removeAllViews()
        view.addView(LayoutInflater.from(view.context).inflate(R.layout.record_view, null))
        timeFormatter = SimpleDateFormat("m:ss", Locale.getDefault())
        val displayMetrics = view.context.resources.displayMetrics
        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels
        isLayoutDirectionRightToLeft = view.context.resources.getBoolean(R.bool.is_right_to_left)
        viewContainer = view.findViewById(R.id.layoutContainer)
        layoutAttachmentOptions = view.findViewById<LinearLayout>(R.id.layoutAttachmentOptions)
        imageViewAttachment = view.findViewById<View>(R.id.imageViewAttachment)
        imageViewCamera = view.findViewById<View>(R.id.imageViewCamera)
        imageViewEmoji = view.findViewById<View>(R.id.imageViewEmoji)
        editTextMessage = view.findViewById(R.id.editTextMessage)
        send = view.findViewById<ImageView>(R.id.imageSend)
        stop = view.findViewById(R.id.imageStop)
        audio = view.findViewById<ImageView>(R.id.imageAudio)
        imageViewAudio = view.findViewById(R.id.imageViewAudio)
        imageViewStop = view.findViewById<View>(R.id.imageViewStop)
        imageViewSend = view.findViewById<View>(R.id.imageViewSend)
        imageViewLock = view.findViewById<View>(R.id.imageViewLock)
        imageViewLockArrow = view.findViewById<View>(R.id.imageViewLockArrow)
        layoutDustin = view.findViewById<View>(R.id.layoutDustin)
        layoutMessage = view.findViewById<View>(R.id.layoutMessage)
        layoutAttachment = view.findViewById(R.id.layoutAttachment)
        textViewSlide = view.findViewById<TextView>(R.id.textViewSlide)
        timeText = view.findViewById(R.id.textViewTime)
        layoutSlideCancel = view.findViewById(R.id.layoutSlideCancel)
        layoutEffect2 = view.findViewById<View>(R.id.layoutEffect2)
        layoutEffect1 = view.findViewById<View>(R.id.layoutEffect1)
        layoutLock = view.findViewById<View>(R.id.layoutLock)
        imageViewMic = view.findViewById<View>(R.id.imageViewMic)
        dustin = view.findViewById<View>(R.id.dustin)
        dustin_cover = view.findViewById<View>(R.id.dustin_cover)
        handler = Handler(Looper.getMainLooper())
        dp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            1f,
            view.context.resources.displayMetrics
        )
        animBlink = AnimationUtils.loadAnimation(
            view.context,
            R.anim.blink
        )
        animJump = AnimationUtils.loadAnimation(
            view.context,
            R.anim.jump
        )
        animJumpFast = AnimationUtils.loadAnimation(
            view.context,
            R.anim.jump_fast
        )
        setupRecording()
        setupAttachmentOptions()
    }

    fun changeSlideToCancelText(textResourceId: Int) {
        textViewSlide.setText(textResourceId)
    }

    fun isShowCameraIcon(): Boolean {
        return showCameraIcon
    }

    fun showCameraIcon(showCameraIcon: Boolean) {
        this.showCameraIcon = showCameraIcon
        if (showCameraIcon) {
            imageViewCamera.setVisibility(View.VISIBLE)
        } else {
            imageViewCamera.setVisibility(View.GONE)
        }
    }

    fun isShowAttachmentIcon(): Boolean {
        return showAttachmentIcon
    }

    fun showAttachmentIcon(showAttachmentIcon: Boolean) {
        this.showAttachmentIcon = showAttachmentIcon
        if (showAttachmentIcon) {
            imageViewAttachment.setVisibility(View.VISIBLE)
        } else {
            imageViewAttachment.setVisibility(View.INVISIBLE)
        }
    }

    fun isShowEmojiIcon(): Boolean {
        return showEmojiIcon
    }

    fun showEmojiIcon(showEmojiIcon: Boolean) {
        this.showEmojiIcon = showEmojiIcon
        if (showEmojiIcon) {
            imageViewEmoji.setVisibility(View.VISIBLE)
        } else {
            imageViewEmoji.setVisibility(View.INVISIBLE)
        }
    }

//    fun setAttachmentOptions(
//        attachmentOptionList: List<AttachmentOption>?,
//        attachmentOptionsListener: AttachmentOptionsListener?
//    ) {
//        this.attachmentOptionList = attachmentOptionList
//        this.attachmentOptionsListener = attachmentOptionsListener
//        if (this.attachmentOptionList != null && !this.attachmentOptionList!!.isEmpty()) {
//            layoutAttachmentOptions.removeAllViews()
//            var count = 0
//            var linearLayoutMain: LinearLayout
//            layoutAttachments = ArrayList()
//            for (attachmentOption in this.attachmentOptionList) {
//                if (count == 6) {
//                    break
//                }
//                if (count == 0 || count == 3) {
//                    linearLayoutMain = LinearLayout(context)
//                    linearLayoutMain.layoutParams =
//                        LinearLayout.LayoutParams(
//                            LinearLayout.LayoutParams.WRAP_CONTENT,
//                            LinearLayout.LayoutParams.WRAP_CONTENT
//                        )
//                    linearLayoutMain.orientation = LinearLayout.HORIZONTAL
//                    linearLayoutMain.gravity = Gravity.CENTER
//                    layoutAttachmentOptions.addView(linearLayoutMain)
//                }
//                val linearLayout = LinearLayout(context)
//                linearLayout.layoutParams = LinearLayout.LayoutParams(
//                    (dp * 84).toInt(),
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                )
//                linearLayout.setPadding(
//                    (dp * 4).toInt(), (dp * 12).toInt(), (dp * 4).toInt(),
//                    (dp * 0).toInt()
//                )
//                linearLayout.orientation = LinearLayout.VERTICAL
//                linearLayout.gravity = Gravity.CENTER
//                layoutAttachments.add(linearLayout)
//                val imageView = ImageView(context)
//                imageView.layoutParams = LinearLayout.LayoutParams(
//                    (dp * 48).toInt(),
//                    (dp * 48).toInt()
//                )
//                imageView.setImageResource(attachmentOption.getResourceImage())
//                val textView = TextView(context)
//                TextViewCompat.setTextAppearance(textView, R.style.TextAttachmentOptions)
//                textView.layoutParams =
//                    LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    )
//                textView.setPadding(
//                    (dp * 4).toInt(), (dp * 4).toInt(), (dp * 4).toInt(),
//                    (dp * 0).toInt()
//                )
//                textView.maxLines = 1
//                textView.setText(attachmentOption.getTitle())
//                linearLayout.addView(imageView)
//                linearLayout.addView(textView)
//                linearLayoutMain!!.addView(linearLayout)
//                linearLayout.setOnClickListener {
//                    hideAttachmentOptionView()
//                    this@AudioRecordView.attachmentOptionsListener.onClick(attachmentOption)
//                }
//                count++
//            }
//        }
//    }

    fun hideAttachmentOptionView() {
        if (layoutAttachment!!.visibility == View.VISIBLE) {
            imageViewAttachment.performClick()
        }
    }

    fun showAttachmentOptionView() {
        if (layoutAttachment!!.visibility != View.VISIBLE) {
            imageViewAttachment.performClick()
        }
    }

    private fun setupAttachmentOptions() {
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

    fun removeAttachmentOptionAnimation(removeAttachmentOptionAnimation: Boolean) {
        this.removeAttachmentOptionAnimation = removeAttachmentOptionAnimation
    }

    fun setContainerView(layoutResourceID: Int): View? {
        val view = LayoutInflater.from(viewContainer!!.context).inflate(layoutResourceID, null)
        if (view == null) {
            showErrorLog("Unable to create the Container View from the layoutResourceID")
            return null
        }
        viewContainer!!.removeAllViews()
        viewContainer!!.addView(view)
        return view
    }

    fun setAudioRecordButtonImage(imageResource: Int) {
        audio.setImageResource(imageResource)
    }

    fun setStopButtonImage(imageResource: Int) {
        stop!!.setImageResource(imageResource)
    }

    fun setSendButtonImage(imageResource: Int) {
        send.setImageResource(imageResource)
    }

    fun getRecordingListener(): RecordingListener? {
        return recordingListener
    }

    fun setRecordingListener(recordingListener: RecordingListener?) {
        if (recordingListener != null) {
            this.recordingListener = recordingListener
        }
    }

    fun getSendView(): View? {
        return imageViewSend
    }

    fun getAttachmentView(): View? {
        return imageViewAttachment
    }

    fun getEmojiView(): View? {
        return imageViewEmoji
    }

    fun getCameraView(): View? {
        return imageViewCamera
    }

    fun getMessageView(): EditText? {
        return editTextMessage
    }

    private fun setupRecording() {
        imageViewSend.animate().scaleX(0f).scaleY(0f).setDuration(100)
            .setInterpolator(LinearInterpolator()).start()
        editTextMessage!!.addTextChangedListener(object : TextWatcher {
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

    private fun translateY(y: Float) {
        if (y < -lockOffset) {
            locked()
            imageViewAudio!!.translationY = 0f
            return
        }
        if (layoutLock.getVisibility() != View.VISIBLE) {
            layoutLock.setVisibility(View.VISIBLE)
        }
        imageViewAudio!!.translationY = y
        layoutLock.setTranslationY(y / 2)
        imageViewAudio!!.translationX = 0f
    }

    private fun translateX(x: Float) {
        if (if (isLayoutDirectionRightToLeft) x > cancelOffset else x < -cancelOffset) {
            canceled()
            imageViewAudio!!.translationX = 0f
            layoutSlideCancel!!.translationX = 0f
            return
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

    private fun locked() {
        stopTrackingAction = true
        stopRecording(RecordingBehaviour.LOCKED)
        isLocked = true
    }

    private fun canceled() {
        stopTrackingAction = true
        stopRecording(RecordingBehaviour.CANCELED)
    }

    private fun stopRecording(recordingBehaviour: RecordingBehaviour) {
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
            return
        }
        if (recordingBehaviour == RecordingBehaviour.LOCKED) {
            imageViewStop.setVisibility(View.VISIBLE)
            if (recordingListener != null) recordingListener!!.onRecordingLocked()
        } else if (recordingBehaviour == RecordingBehaviour.CANCELED) {
            timeText!!.clearAnimation()
            timeText!!.visibility = View.INVISIBLE
            imageViewMic.setVisibility(View.INVISIBLE)
            imageViewStop.setVisibility(View.GONE)
            layoutEffect2.setVisibility(View.GONE)
            layoutEffect1.setVisibility(View.GONE)
            timerTask!!.cancel()
            delete()
            if (recordingListener != null) recordingListener!!.onRecordingCanceled()
        } else if (recordingBehaviour == RecordingBehaviour.RELEASED || recordingBehaviour == RecordingBehaviour.LOCK_DONE) {
            timeText!!.clearAnimation()
            timeText!!.visibility = View.INVISIBLE
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

    private fun startRecord() {
        if (recordingListener != null) recordingListener!!.onRecordingStarted()
        hideAttachmentOptionView()
        stopTrackingAction = false
        editTextMessage!!.visibility = View.INVISIBLE
        imageViewAttachment.setVisibility(View.INVISIBLE)
        imageViewCamera.setVisibility(View.INVISIBLE)
        imageViewEmoji.setVisibility(View.INVISIBLE)
        imageViewAudio!!.animate().scaleXBy(1f).scaleYBy(1f).setDuration(200)
            .setInterpolator(OvershootInterpolator()).start()
        timeText!!.visibility = View.VISIBLE
        layoutLock.setVisibility(View.VISIBLE)
        layoutSlideCancel!!.visibility = View.VISIBLE
        imageViewMic.setVisibility(View.VISIBLE)
        layoutEffect2.setVisibility(View.VISIBLE)
        layoutEffect1.setVisibility(View.VISIBLE)
        timeText!!.startAnimation(animBlink)
        imageViewLockArrow.clearAnimation()
        imageViewLock.clearAnimation()
        imageViewLockArrow.startAnimation(animJumpFast)
        imageViewLock.startAnimation(animJump)
        if (audioTimer == null) {
            audioTimer = Timer()
            timeFormatter!!.timeZone = TimeZone.getTimeZone("UTC")
        }
        timerTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    timeText.text = ""
                    audioTotalTime++
                }
            }
        }
        audioTotalTime = 0
        audioTimer!!.schedule(timerTask, 0, 1000)
    }

    private fun delete() {
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
                    dustin_cover.setTranslationX(displacement)
                    dustin_cover.animate().translationX(0f).rotation(-120f).setDuration(350)
                        .setInterpolator(
                            DecelerateInterpolator()
                        ).start()
                    dustin.animate().translationX(0f).setDuration(350).setInterpolator(
                        DecelerateInterpolator()
                    ).setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            dustin.setVisibility(View.VISIBLE)
                            dustin_cover.setVisibility(View.VISIBLE)
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
                                    dustin_cover.animate().rotation(0f).setDuration(150).setStartDelay(50)
                                        .start()
                                    dustin.animate().translationX(displacement).setDuration(200)
                                        .setStartDelay(250).setInterpolator(
                                            DecelerateInterpolator()
                                        ).start()
                                    dustin_cover.animate().translationX(displacement).setDuration(200)
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

}