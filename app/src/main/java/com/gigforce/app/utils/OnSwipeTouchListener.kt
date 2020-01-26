package com.gigforce.app.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

private const val SWIPE_THRESHOLD:Int = 100
private const val SWIPE_VELOCITY_THRESHOLD = 100

class OnSwipeTouchListener(val context:Context, val swipeListner:SimpleSwipeGestureListener)
    :View.OnTouchListener
{
    lateinit final var gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, GestureListener(swipeListner));
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event);
    }

    interface SimpleSwipeGestureListener{
        fun onSwipeRight()
        fun onSwipeLeft()
        fun onSwipeTop()
        fun onSwipeBottom()
    }

    private final class GestureListener(val swipeListner:SimpleSwipeGestureListener)
        : GestureDetector.SimpleOnGestureListener()
    {
        override fun onDown(e: MotionEvent?): Boolean {
            return super.onDown(e)
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY = e2!!.y - e1!!.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            swipeListner.onSwipeRight()
                        } else {
                            swipeListner.onSwipeLeft()
                        }
                        result = true
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(
                        velocityY
                    ) > SWIPE_VELOCITY_THRESHOLD
                ) {
                    if (diffY > 0) {
                        swipeListner.onSwipeBottom()
                    } else {
                        swipeListner.onSwipeTop()
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }


    }
}