package com.gigforce.lead_management.ui.joining_list_2.views

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.widget.HorizontalScrollView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.gigforce.lead_management.R
import com.google.android.material.button.MaterialButton
import timber.log.Timber

class JoiningsButtonTabLayout : HorizontalScrollView {

    companion object {

        const val POSITION_ALL = 0
        const val POSITION_PENDING = 1
        const val POSITION_COMPLETED = 2
    }

    //Constructors
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private var allButton: MaterialButton
    private var pendingButton: MaterialButton
    private var completedButton: MaterialButton

    private var mCurrentlySelectedPosition = -1
    private var mOnTabClickListener: ((Int) -> Unit)? = null

    private val onTabButtonClickListener = OnClickListener { clickedButton ->

        when (clickedButton.id) {
            R.id.tab_all -> {
                setItemAsSelected(POSITION_ALL)
                mOnTabClickListener?.invoke(POSITION_ALL)
            }
            R.id.tab_pending -> {
                setItemAsSelected(POSITION_PENDING)
                mOnTabClickListener?.invoke(POSITION_PENDING)
            }
            R.id.tab_completed -> {
                setItemAsSelected(POSITION_COMPLETED)
                mOnTabClickListener?.invoke(POSITION_COMPLETED)
            }
        }
    }

    init {
        inflate(context, R.layout.view_tablayout_joinings_2, this)

        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false

        allButton = findViewById(R.id.tab_all)
        completedButton = findViewById(R.id.tab_completed)
        pendingButton = findViewById(R.id.tab_pending)

        this.allButton.setOnClickListener(onTabButtonClickListener)
        this.completedButton.setOnClickListener(onTabButtonClickListener)
        this.pendingButton.setOnClickListener(onTabButtonClickListener)

        setItemAsSelected(POSITION_ALL)
    }

    fun setOnTabClickListener(listener: (Int) -> Unit) {
        this.mOnTabClickListener = listener
    }

    fun setItemAsSelected(position: Int) {
        Timber.d("Current Pos : $mCurrentlySelectedPosition")
        Timber.d("New Pos received : $position")

        if (mCurrentlySelectedPosition == position) {
            //no-op
            return
        }

        when (position) {
            POSITION_ALL -> {
                unselectedButton(pendingButton)
                unselectedButton(completedButton)
                selectedButton(allButton)
            }
            POSITION_COMPLETED -> {
                unselectedButton(pendingButton)
                unselectedButton(allButton)
                selectedButton(completedButton)
            }
            POSITION_PENDING -> {
                unselectedButton(allButton)
                unselectedButton(completedButton)
                selectedButton(pendingButton)
            }
            else -> {
                //no-op
            }
        }

        this.mCurrentlySelectedPosition = position
    }

    private fun selectedButton(button: MaterialButton) {
        ViewCompat.setElevation(button, 8.0f)
        button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.lipstick_2)
        button.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))
    }

    private fun unselectedButton(button: MaterialButton) {
        ViewCompat.setElevation(button, 12.0f)
        button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.white)
        button.setTextColor(ResourcesCompat.getColor(context.resources, R.color.black, null))
    }
}