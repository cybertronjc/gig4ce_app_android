package com.gigforce.modules.feature_chat.swipe

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.DisplayUtil
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.core.CoreViewHolder
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.ui.chatItems.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MessageSwipeController(
    private val context: Context,
    private val swipeControllerActions: SwipeControllerActions
) : ItemTouchHelper.Callback() {

    private val currentUser : FirebaseUser? by lazy {
        FirebaseAuth.getInstance().currentUser
    }

    private lateinit var imageDrawable: Drawable
    private var currentItemViewHolder: RecyclerView.ViewHolder? = null
    private lateinit var mView: View
    private var dX = 0f

    private var replyButtonProgress: Float = 0.toFloat()
    private var lastReplyButtonAnimationTime: Long = 0
    private var swipeBack = false
    private var isVibrate = false
    private var startTracking = false
    private var swipeEnabled = true

    fun enableSwipe() {
        swipeEnabled = true
    }

    fun disableSwipe() {
        swipeEnabled = false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return swipeEnabled
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        mView = viewHolder.itemView
        imageDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_reply_back_24dp)!!

        if (viewHolder is CoreViewHolder &&
            viewHolder.itemView is BaseChatMessageItemView &&
            (viewHolder.itemView is TextMessageView
                    || viewHolder.itemView is ImageMessageView
                    || viewHolder.itemView is VideoMessageView
                    || viewHolder.itemView is DocumentMessageView
                    || viewHolder.itemView is LocationMessageView
            )
        ) {

            try {
                val chatMessage =
                    (viewHolder.itemView as BaseChatMessageItemView).getCurrentChatMessageOrThrow()

                if (chatMessage.type == ChatConstants.CHAT_TYPE_USER && chatMessage.flowType != ChatConstants.FLOW_TYPE_IN
                        || (chatMessage.type == ChatConstants.CHAT_TYPE_GROUP && chatMessage.senderInfo.id == currentUser?.uid)
                ) {
                    // Disabling swipe for outgoing message
                    return makeMovementFlags(ACTION_STATE_IDLE, 0)
                }

                return makeMovementFlags(ACTION_STATE_IDLE, RIGHT)
            } catch (e: Exception) {
                e.printStackTrace()
                return makeMovementFlags(ACTION_STATE_IDLE, 0)
            }
        } else {
            return makeMovementFlags(ACTION_STATE_IDLE, 0)
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        if (actionState == ACTION_STATE_SWIPE) {
            setTouchListener(recyclerView, viewHolder)
        }

        if (mView.translationX < convertTodp(130) || dX < this.dX) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            this.dX = dX
            startTracking = true
        }
        currentItemViewHolder = viewHolder
        drawReplyButton(c)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        recyclerView.setOnTouchListener { _, event ->
            swipeBack =
                event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            if (swipeBack) {
                if (Math.abs(mView.translationX) >= this@MessageSwipeController.convertTodp(100)) {

                    if (viewHolder is CoreViewHolder &&
                        viewHolder.itemView is BaseChatMessageItemView
                    ) {

                        try {
                            val message =
                                (viewHolder.itemView as BaseChatMessageItemView).getCurrentChatMessageOrThrow()
                            swipeControllerActions.showReplyUI(message)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            false
        }
    }

    private fun drawReplyButton(canvas: Canvas) {
        if (currentItemViewHolder == null) {
            return
        }
        val translationX = mView.translationX
        val newTime = System.currentTimeMillis()
        val dt = Math.min(17, newTime - lastReplyButtonAnimationTime)
        lastReplyButtonAnimationTime = newTime
        val showing = translationX >= convertTodp(30)
        if (showing) {
            if (replyButtonProgress < 1.0f) {
                replyButtonProgress += dt / 180.0f
                if (replyButtonProgress > 1.0f) {
                    replyButtonProgress = 1.0f
                } else {
                    mView.invalidate()
                }
            }
        } else if (translationX <= 0.0f) {
            replyButtonProgress = 0f
            startTracking = false
            isVibrate = false
        } else {
            if (replyButtonProgress > 0.0f) {
                replyButtonProgress -= dt / 180.0f
                if (replyButtonProgress < 0.1f) {
                    replyButtonProgress = 0f
                } else {
                    mView.invalidate()
                }
            }
        }
        val alpha: Int
        val scale: Float
        if (showing) {
            scale = if (replyButtonProgress <= 0.8f) {
                1.2f * (replyButtonProgress / 0.8f)
            } else {
                1.2f - 0.2f * ((replyButtonProgress - 0.8f) / 0.2f)
            }
            alpha = Math.min(255f, 255 * (replyButtonProgress / 0.8f)).toInt()
        } else {
            scale = replyButtonProgress
            alpha = Math.min(255f, 255 * replyButtonProgress).toInt()
        }
        imageDrawable.alpha = alpha
        if (startTracking) {
            if (!isVibrate && mView.translationX >= convertTodp(100)) {
                mView.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
                isVibrate = true
            }
        }

        val x: Int = if (mView.translationX > convertTodp(130)) {
            convertTodp(130) / 2
        } else {
            (mView.translationX / 2).toInt()
        }

        val y = (mView.top + mView.measuredHeight / 2).toFloat()
        imageDrawable.setBounds(
            (x - convertTodp(12) * scale).toInt(),
            (y - convertTodp(11) * scale).toInt(),
            (x + convertTodp(12) * scale).toInt(),
            (y + convertTodp(10) * scale).toInt()
        )
        imageDrawable.draw(canvas)
        imageDrawable.alpha = 255
    }

    private fun convertTodp(pixel: Int): Int {
        return DisplayUtil.toDp(pixel.toFloat(), context)
    }

}