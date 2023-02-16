package com.flok22.android.agicent.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

internal open class MyGestureDetector(context: Context) : View.OnTouchListener {
    private val gestureDetector: GestureDetector

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        view.performClick()
        return gestureDetector.onTouchEvent(motionEvent)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private val swipeThreshold = 100
        private val swipeThresholdVelocity = 100
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick()
            return super.onSingleTapUp(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleClick()
            return super.onDoubleTap(e)
        }

        override fun onLongPress(e: MotionEvent) {
            onLongClick()
            super.onLongPress(e)
        }

        override fun onFling(
            e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float
        ): Boolean {
            try {
                val diffY = e2.y - e1.y
                if (abs(diffY) > swipeThreshold && abs(
                        velocityY
                    ) > swipeThresholdVelocity
                ) {
                    if (diffY < 0) {
                        onSwipeUp()
                    } else {
                        onSwipeDown()
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return false
        }
    }

    open fun onSwipeUp() {}
    open fun onSwipeDown() {}
    private fun onClick() {}
    private fun onDoubleClick() {}
    private fun onLongClick() {}

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }
}