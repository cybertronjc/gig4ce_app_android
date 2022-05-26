package com.gigforce.app.tl_work_space.home.views

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.ActionsAttachmentViewLayoutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActionsAttachmentView(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs)
    {
        companion object {
            private const val TAG = "ActionsAttachmentView"
        }

         var viewBinding: ActionsAttachmentViewLayoutBinding

        private lateinit var attachmentOptionsListener: ActionAttachmentOptionsListetner

        private var animBlink: Animation? = null
        private var animJump: Animation? = null
        private var animJumpFast: Animation? = null
        private lateinit var attachmentAllOptionList: List<ActionsAttachmentOption>
        private lateinit var attachmentQuickOptionList: List<ActionsAttachmentOption>
        private var layoutAllActionsAttachments: MutableList<LinearLayout> = arrayListOf()
        private var layoutQuickActionsAttachments: MutableList<LinearLayout> = arrayListOf()

        var isLayoutDirectionRightToLeft = false

        var screenWidth = 0
        var screenHeight:Int = 0
        private var dp = 0f

        private val removeAttachmentOptionAnimation = false

        init {
            viewBinding = ActionsAttachmentViewLayoutBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )

            setUpViews()
        }

        private fun setUpViews() {
            val displayMetrics = context.resources.displayMetrics
            screenHeight = displayMetrics.heightPixels
            screenWidth = displayMetrics.widthPixels
            isLayoutDirectionRightToLeft = context.resources.getBoolean(R.bool.is_right_to_left)

            dp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1f,
                context.resources.displayMetrics
            )
            animBlink = AnimationUtils.loadAnimation(
                context,
                R.anim.blink_tl
            )
            animJump = AnimationUtils.loadAnimation(
                context,
                R.anim.jump_tl
            )
            animJumpFast = AnimationUtils.loadAnimation(
                context,
                R.anim.jump_fast_tl
            )

            setupAttachmentOptions()
        }

        private fun setupAttachmentOptions() = viewBinding.apply{
            actionsFab.setOnClickListener(View.OnClickListener {
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
                    attachmentOptionsListener.isVisible(visible = false)
                } else {
                    if (!removeAttachmentOptionAnimation) {
                        var count = 0
                        if (layoutAllActionsAttachments != null && !layoutAllActionsAttachments!!.isEmpty()) {
                            var arr = intArrayOf(5, 4, 2, 3, 1, 0)
                            if (isLayoutDirectionRightToLeft) {
                                arr = intArrayOf(3, 4, 0, 5, 1, 2)
                            }
                            for (i in layoutAllActionsAttachments!!.indices) {
                                if (arr[i] < layoutAllActionsAttachments!!.size) {
                                    val layout = layoutAllActionsAttachments!![arr[i]]
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
                    attachmentOptionsListener.isVisible(visible = true)
                }
            })
        }

        fun setAttachmentOptions(
            attachmentAllOptionList: List<ActionsAttachmentOption>?,
            attachmentQuickOptionList: List<ActionsAttachmentOption>?,
            attachmentOptionsListetner: ActionAttachmentOptionsListetner
        ) = viewBinding.apply {
            if (attachmentAllOptionList != null) {
                this@ActionsAttachmentView.attachmentAllOptionList = attachmentAllOptionList
            }
            if (attachmentQuickOptionList != null){
                this@ActionsAttachmentView.attachmentQuickOptionList = attachmentQuickOptionList
            }
            this@ActionsAttachmentView.attachmentOptionsListener = attachmentOptionsListetner!!
            if (this@ActionsAttachmentView.attachmentQuickOptionList != null && !this@ActionsAttachmentView.attachmentQuickOptionList!!.isEmpty()) {
                layoutAttachmentQuickActions.removeAllViews()
                var count = 0
                var linearLayoutMain: LinearLayout? = null
                for (attachmentOption in this@ActionsAttachmentView.attachmentQuickOptionList) {
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
                        linearLayoutMain.elevation = 12F
                        layoutAttachmentQuickActions.addView(linearLayoutMain)
                    }
                    val itemView = LayoutInflater.from(context).inflate(R.layout.action_card_item_layout, null)
                    val actionTextView: TextView = itemView.findViewById(R.id.action_name_tv)
                    val actionImageView: ImageView = itemView.findViewById(R.id.action_image_iv)


                    actionImageView.setImageResource(attachmentOption.resourceImage)
                    actionTextView.setText(attachmentOption.title)

                    itemView.setOnClickListener {
                        hideAttachmentOptionView()
                        this@ActionsAttachmentView.attachmentOptionsListener.onClick(attachmentOption)
                    }

                    linearLayoutMain?.addView(itemView)
                    count++
                }
            }
            //this@ActionsAttachmentView.attachmentOptionsListener = attachmentOptionsListener!!
            if (this@ActionsAttachmentView.attachmentAllOptionList != null && !this@ActionsAttachmentView.attachmentAllOptionList!!.isEmpty()) {
                layoutAttachmentAllActions.removeAllViews()
                var count = 0
                var linearLayoutMain: LinearLayout? = null
                for (attachmentOption in this@ActionsAttachmentView.attachmentAllOptionList) {
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
                        layoutAttachmentAllActions.addView(linearLayoutMain)
                    }
                    val itemView = LayoutInflater.from(context).inflate(R.layout.action_card_item_layout, null)
                    val actionTextView: TextView = itemView.findViewById(R.id.action_name_tv)
                    val actionImageView: ImageView = itemView.findViewById(R.id.action_image_iv)

                    actionImageView.setImageResource(attachmentOption.resourceImage)
                    actionTextView.setText(attachmentOption.title)

                    itemView.setOnClickListener {
                        hideAttachmentOptionView()
                        this@ActionsAttachmentView.attachmentOptionsListener.onClick(attachmentOption)
                    }

                    linearLayoutMain?.addView(itemView)
                    count++
                }
            }
        }

        fun hideAttachmentOptionView() {
            if (viewBinding.layoutAttachment!!.visibility == View.VISIBLE) {
                viewBinding.actionsFab.performClick()
            }
        }

        fun isAttachmentOptionViewVisible(): Boolean {
            return viewBinding.layoutAttachment.isVisible
        }

    }