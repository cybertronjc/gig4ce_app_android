//package com.gigforce.app.modules.homescreen
//
//import android.content.Context
//import android.os.Parcel
//import android.os.Parcelable
//import android.support.annotation.IntDef
//import android.support.annotation.RestrictTo
//import android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP
//import android.support.annotation.VisibleForTesting
//import android.support.design.widget.CoordinatorLayout
//import android.support.v4.os.ParcelableCompat
//import android.support.v4.os.ParcelableCompatCreatorCallbacks
//import android.support.v4.view.AbsSavedState
//import android.support.v4.view.MotionEventCompat
//import android.support.v4.view.NestedScrollingChild
//import android.support.v4.view.VelocityTrackerCompat
//import android.support.v4.view.ViewCompat
//import android.support.v4.widget.ViewDragHelper
//import android.util.AttributeSet
//import android.view.*
//import com.riningan.widget.extendedbottomsheetbehavior.R
//import java.lang.annotation.Retention
//import java.lang.annotation.RetentionPolicy
//import java.lang.ref.WeakReference
//
//package com.riningan.widget
//import android.support.annotation.IntDef
//import android.support.annotation.RestrictTo
//import android.support.annotation.VisibleForTesting
//import android.support.design.widget.CoordinatorLayout
//import android.support.v4.os.ParcelableCompat
//import android.support.v4.os.ParcelableCompatCreatorCallbacks
//import android.support.v4.view.AbsSavedState
//import android.support.v4.view.MotionEventCompat
//import android.support.v4.view.NestedScrollingChild
//import android.support.v4.view.VelocityTrackerCompat
//import android.support.v4.view.ViewCompat
//import android.support.v4.widget.ViewDragHelper
//import com.riningan.widget.extendedbottomsheetbehavior.R
//import android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP
//
///**
// * Created by Vadim Akhmarov on 17.07.2017.
// * Project ExtendedBottomSheetBehaviorProject
// * Classname ExtendedBottomSheetBehavior
// * Version 1.0
// * Copyright All rights reserved.
// */
//class ExtendedBottomSheetBehavior<V : View?> : CoordinatorLayout.Behavior<V> {
//    /**
//     * Callback for monitoring events about bottom sheets.
//     */
//    abstract class BottomSheetCallback {
//        /**
//         * Called when the bottom sheet changes its state.
//         *
//         * @param bottomSheet The bottom sheet view.
//         * @param newState    The new state. This will be one of [.STATE_DRAGGING],
//         * [.STATE_SETTLING], [.STATE_EXPANDED], [.STATE_HALF],
//         * [.STATE_COLLAPSED], or [.STATE_HIDDEN].
//         */
//        abstract fun onStateChanged(
//            @NonNull bottomSheet: View?,
//            @com.riningan.widget.ExtendedBottomSheetBehavior.State newState: Int
//        )
//
//        /**
//         * Called when the bottom sheet is being dragged.
//         *
//         * @param bottomSheet The bottom sheet view.
//         * @param slideOffset The new offset of this bottom sheet within [-1,1] range. Offset
//         * increases as this bottom sheet is moving upward. From 0 to 1 the sheet
//         * is between collapsed and expanded states and from -1 to 0 it is
//         * between hidden and collapsed states.
//         */
//        abstract fun onSlide(
//            @NonNull bottomSheet: View?,
//            slideOffset: Float
//        )
//    }
//
//    /**
//     * @hide
//     */
//    @RestrictTo(LIBRARY_GROUP)
//    @IntDef([STATE_EXPANDED, STATE_HALF, STATE_COLLAPSED, STATE_DRAGGING, STATE_SETTLING, STATE_HIDDEN])
//    @Retention(RetentionPolicy.SOURCE)
//    annotation class State
//
//    private var mMaximumVelocity = 0f
//    private var mPeekHeight = 0
//    private var mPeekHeightAuto = false
//
//    @get:VisibleForTesting
//    private var peekHeightMin = 0
//    private var mHalfOffset = 0
//    private var mMinOffset = 0
//    private var mMaxOffset = 0
//    /**
//     * Gets whether this bottom sheet can hide when it is swiped down.
//     *
//     * @return `true` if this bottom sheet can hide.
//     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
//     */
//    /**
//     * Sets whether this bottom sheet can hide when it is swiped down.
//     *
//     * @param hideable `true` to make this bottom sheet hideable.
//     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
//     */
//    var isHideable = false
//    /**
//     * Sets whether this bottom sheet should skip the collapsed state when it is being hidden
//     * after it is expanded once.
//     *
//     * @return Whether the bottom sheet should skip the collapsed state.
//     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
//     */
//    /**
//     * Sets whether this bottom sheet should skip the collapsed state when it is being hidden
//     * after it is expanded once. Setting this to true has no effect unless the sheet is hideable.
//     *
//     * @param skipCollapsed True if the bottom sheet should skip the collapsed state.
//     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
//     */
//    var skipCollapsed = false
//
//    @com.riningan.widget.ExtendedBottomSheetBehavior.State
//    private var mState = STATE_COLLAPSED
//    private var mViewDragHelper: ViewDragHelper? = null
//    private var mIgnoreEvents = false
//    private var mLastNestedScrollDy = 0
//    private var mNestedScrolled = false
//    private var mParentHeight = 0
//    private var mViewRef: WeakReference<V>? = null
//    private var mNestedScrollingChildRef: WeakReference<View?>? = null
//    private var mCallback: BottomSheetCallback? = null
//    private var mVelocityTracker: VelocityTracker? = null
//    private var mActivePointerId = 0
//    private var mInitialY = 0
//    private var mTouchingScrollingChild = false
//    /**
//     * Is allow dragging.
//     *
//     * @return
//     */
//    /**
//     * Allow dragging.
//     *
//     * @param
//     */
//    var isAllowUserDragging = false
//
//    /**
//     * Default constructor for instantiating BottomSheetBehaviors.
//     */
//    constructor() {}
//
//    /**
//     * Default constructor for inflating BottomSheetBehaviors from layout.
//     *
//     * @param context The [Context].
//     * @param attrs   The [AttributeSet].
//     */
//    constructor(
//        context: Context,
//        attrs: AttributeSet?
//    ) : super(context, attrs) {
//        val a =
//            context.obtainStyledAttributes(attrs, R.styleable.ExtendedBottomSheetBehavior)
//        val value = a.peekValue(R.styleable.ExtendedBottomSheetBehavior_peekHeight)
//        peekHeight = if (value != null && value.data == PEEK_HEIGHT_AUTO) {
//            value.data
//        } else {
//            a.getDimensionPixelSize(
//                R.styleable.ExtendedBottomSheetBehavior_peekHeight,
//                PEEK_HEIGHT_AUTO
//            )
//        }
//        hideable = a.getBoolean(R.styleable.ExtendedBottomSheetBehavior_hideable, false)
//        mHalfOffset = a.getDimensionPixelSize(R.styleable.ExtendedBottomSheetBehavior_halfOffset, 0)
//        isAllowUserDragging =
//            a.getBoolean(R.styleable.ExtendedBottomSheetBehavior_allowDragging, true)
//        skipCollapsed = a.getBoolean(R.styleable.ExtendedBottomSheetBehavior_skipCollapsed, false)
//        a.recycle()
//        val configuration = ViewConfiguration.get(context)
//        mMaximumVelocity = configuration.scaledMaximumFlingVelocity.toFloat()
//    }
//
//    fun onSaveInstanceState(@NonNull parent: CoordinatorLayout?, @NonNull child: V): Parcelable {
//        return com.riningan.widget.ExtendedBottomSheetBehavior.SavedState(
//            super.onSaveInstanceState(
//                parent,
//                child
//            ), mState
//        )
//    }
//
//    fun onRestoreInstanceState(
//        @NonNull parent: CoordinatorLayout?,
//        @NonNull child: V,
//        @NonNull state: Parcelable
//    ) {
//        val ss: com.riningan.widget.ExtendedBottomSheetBehavior.SavedState =
//            state as com.riningan.widget.ExtendedBottomSheetBehavior.SavedState
//        super.onRestoreInstanceState(parent, child, ss.getSuperState())
//        // Intermediate states are restored as collapsed state
//        mState = if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
//            STATE_COLLAPSED
//        } else {
//            ss.state
//        }
//    }
//
//    fun onLayoutChild(
//        @NonNull parent: CoordinatorLayout,
//        @NonNull child: V,
//        layoutDirection: Int
//    ): Boolean {
//        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
//            ViewCompat.setFitsSystemWindows(child, true)
//        }
//        val savedTop = child!!.top
//        // First let the parent lay it out
//        parent.onLayoutChild(child, layoutDirection)
//        // Offset the bottom sheet
//        mParentHeight = parent.getHeight()
//        val peekHeight: Int
//        if (mPeekHeightAuto) {
//            if (peekHeightMin == 0) {
//                peekHeightMin = parent.getResources()
//                    .getDimensionPixelSize(android.support.design.R.dimen.design_bottom_sheet_peek_height_min)
//            }
//            peekHeight =
//                Math.max(peekHeightMin, mParentHeight - parent.getWidth() * 9 / 16)
//        } else {
//            peekHeight = mPeekHeight
//        }
//        mMinOffset = Math.max(0, mParentHeight - child.height)
//        mMaxOffset = Math.max(mParentHeight - peekHeight, mMinOffset)
//        if (mState == STATE_EXPANDED) {
//            ViewCompat.offsetTopAndBottom(child, mMinOffset)
//        } else if (mState == STATE_HALF) {
//            ViewCompat.offsetTopAndBottom(child, mHalfOffset)
//        } else if (isHideable && mState == STATE_HIDDEN) {
//            ViewCompat.offsetTopAndBottom(child, mParentHeight)
//        } else if (mState == STATE_COLLAPSED) {
//            ViewCompat.offsetTopAndBottom(child, mMaxOffset)
//        } else if (mState == STATE_DRAGGING || mState == STATE_SETTLING) {
//            ViewCompat.offsetTopAndBottom(child, savedTop - child.top)
//        }
//        if (mViewDragHelper == null) {
//            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback)
//        }
//        mViewRef = WeakReference(child)
//        mNestedScrollingChildRef =
//            WeakReference(findScrollingChild(child))
//        return true
//    }
//
//    fun onInterceptTouchEvent(
//        @NonNull parent: CoordinatorLayout,
//        @NonNull child: V,
//        @NonNull event: MotionEvent
//    ): Boolean {
//        if (!isAllowUserDragging) {
//            return false
//        }
//        if (!child!!.isShown) {
//            mIgnoreEvents = true
//            return false
//        }
//        val action: Int = MotionEventCompat.getActionMasked(event)
//        // Record the velocity
//        if (action == MotionEvent.ACTION_DOWN) {
//            reset()
//        }
//        if (mVelocityTracker == null) {
//            mVelocityTracker = VelocityTracker.obtain()
//        }
//        mVelocityTracker!!.addMovement(event)
//        when (action) {
//            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                mTouchingScrollingChild = false
//                mActivePointerId = MotionEvent.INVALID_POINTER_ID
//                // Reset the ignore flag
//                if (mIgnoreEvents) {
//                    mIgnoreEvents = false
//                    return false
//                }
//            }
//            MotionEvent.ACTION_DOWN -> {
//                val initialX = event.x.toInt()
//                mInitialY = event.y.toInt()
//                val scroll = mNestedScrollingChildRef!!.get()
//                if (scroll != null && parent.isPointInChildBounds(scroll, initialX, mInitialY)) {
//                    mActivePointerId = event.getPointerId(event.actionIndex)
//                    mTouchingScrollingChild = true
//                }
//                mIgnoreEvents =
//                    mActivePointerId == MotionEvent.INVALID_POINTER_ID && !parent.isPointInChildBounds(
//                        child,
//                        initialX,
//                        mInitialY
//                    )
//            }
//        }
//        if (!mIgnoreEvents && mViewDragHelper.shouldInterceptTouchEvent(event)) {
//            return true
//        }
//        // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
//        // it is not the top most view of its parent. This is not necessary when the touch event is
//        // happening over the scrolling content as nested scrolling logic handles that case.
//        val scroll = mNestedScrollingChildRef!!.get()
//        return action == MotionEvent.ACTION_MOVE && scroll != null &&
//                !mIgnoreEvents && mState != STATE_DRAGGING &&
//                !parent.isPointInChildBounds(scroll, event.x.toInt(), event.y.toInt()) && Math.abs(
//            mInitialY - event.y
//        ) > mViewDragHelper.getTouchSlop()
//    }
//
//    fun onTouchEvent(
//        @NonNull parent: CoordinatorLayout?,
//        @NonNull child: V,
//        @NonNull event: MotionEvent
//    ): Boolean {
//        if (!isAllowUserDragging) {
//            return false
//        }
//        if (!child!!.isShown) {
//            return false
//        }
//        val action: Int = MotionEventCompat.getActionMasked(event)
//        if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
//            return true
//        }
//        if (mViewDragHelper == null) {
//            return false
//        }
//        mViewDragHelper.processTouchEvent(event)
//        // Record the velocity
//        if (action == MotionEvent.ACTION_DOWN) {
//            reset()
//        }
//        if (mVelocityTracker == null) {
//            mVelocityTracker = VelocityTracker.obtain()
//        }
//        mVelocityTracker!!.addMovement(event)
//        // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
//        // to capture the bottom sheet in case it is not captured and the touch slop is passed.
//        if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents) {
//            if (Math.abs(mInitialY - event.y) > mViewDragHelper.getTouchSlop()) {
//                mViewDragHelper.captureChildView(child, event.getPointerId(event.actionIndex))
//            }
//        }
//        return !mIgnoreEvents
//    }
//
//    fun onStartNestedScroll(
//        @NonNull coordinatorLayout: CoordinatorLayout?,
//        @NonNull child: V,
//        @NonNull directTargetChild: View?,
//        @NonNull target: View?,
//        nestedScrollAxes: Int
//    ): Boolean {
//        if (!isAllowUserDragging) {
//            return false
//        }
//        mLastNestedScrollDy = 0
//        mNestedScrolled = false
//        return nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL !== 0
//    }
//
//    fun onNestedPreScroll(
//        @NonNull coordinatorLayout: CoordinatorLayout?,
//        @NonNull child: V,
//        @NonNull target: View,
//        dx: Int,
//        dy: Int,
//        @NonNull consumed: IntArray
//    ) {
//        if (!isAllowUserDragging) {
//            return
//        }
//        val scrollingChild = mNestedScrollingChildRef!!.get()
//        if (target !== scrollingChild) {
//            return
//        }
//        val currentTop = child!!.top
//        val newTop = currentTop - dy
//        if (dy > 0) { // Upward
//            if (newTop < mMinOffset) {
//                consumed[1] = currentTop - mMinOffset
//                ViewCompat.offsetTopAndBottom(child, -consumed[1])
//                if (currentTop < mHalfOffset) {
//                    setStateInternal(STATE_EXPANDED)
//                } else {
//                    setStateInternal(STATE_HALF)
//                }
//            } else {
//                consumed[1] = dy
//                ViewCompat.offsetTopAndBottom(child, -dy)
//                setStateInternal(STATE_DRAGGING)
//            }
//        } else if (dy < 0) { // Downward
//            if (!ViewCompat.canScrollVertically(target, -1)) {
//                if (newTop <= mMaxOffset || isHideable) {
//                    consumed[1] = dy
//                    ViewCompat.offsetTopAndBottom(child, -dy)
//                    setStateInternal(STATE_DRAGGING)
//                } else {
//                    consumed[1] = currentTop - mMaxOffset
//                    ViewCompat.offsetTopAndBottom(child, -consumed[1])
//                    if (currentTop < mHalfOffset) {
//                        setStateInternal(STATE_HALF)
//                    } else {
//                        setStateInternal(STATE_COLLAPSED)
//                    }
//                }
//            }
//        }
//        dispatchOnSlide(child.top)
//        mLastNestedScrollDy = dy
//        mNestedScrolled = true
//    }
//
//    fun onStopNestedScroll(
//        @NonNull coordinatorLayout: CoordinatorLayout?,
//        @NonNull child: V,
//        @NonNull target: View
//    ) {
//        if (!isAllowUserDragging) {
//            return
//        }
//        if (child!!.top == mMinOffset) {
//            setStateInternal(STATE_EXPANDED)
//            return
//        }
//        if (target !== mNestedScrollingChildRef!!.get() || !mNestedScrolled) {
//            return
//        }
//        val top: Int
//        val targetState: Int
//        if (mLastNestedScrollDy > 0) {
//            if (child.top < mHalfOffset) {
//                top = mMinOffset
//                targetState = STATE_EXPANDED
//            } else {
//                top = mHalfOffset
//                targetState = STATE_HALF
//            }
//        } else if (isHideable && shouldHide(child, yVelocity)) {
//            top = mParentHeight
//            targetState = STATE_HIDDEN
//        } else if (mLastNestedScrollDy == 0) {
//            val currentTop = child.top
//            if (Math.abs(currentTop - mMinOffset) < Math.abs(currentTop - mMaxOffset)) {
//                if (currentTop < mHalfOffset) {
//                    top = mHalfOffset
//                    targetState = STATE_HALF
//                } else {
//                    top = mMinOffset
//                    targetState = STATE_EXPANDED
//                }
//            } else {
//                top = mMaxOffset
//                targetState = STATE_COLLAPSED
//            }
//        } else {
//            if (child.top < mHalfOffset) {
//                top = mHalfOffset
//                targetState = STATE_HALF
//            } else {
//                top = mMaxOffset
//                targetState = STATE_COLLAPSED
//            }
//        }
//        if (mViewDragHelper.smoothSlideViewTo(child, child.left, top)) {
//            setStateInternal(STATE_SETTLING)
//            ViewCompat.postOnAnimation(
//                child,
//                com.riningan.widget.ExtendedBottomSheetBehavior.SettleRunnable(child, targetState)
//            )
//        } else {
//            setStateInternal(targetState)
//        }
//        mNestedScrolled = false
//    }
//
//    fun onNestedPreFling(
//        @NonNull coordinatorLayout: CoordinatorLayout?,
//        @NonNull child: V,
//        @NonNull target: View,
//        velocityX: Float,
//        velocityY: Float
//    ): Boolean {
//        return if (!isAllowUserDragging) {
//            false
//        } else target === mNestedScrollingChildRef!!.get() && (mState != STATE_EXPANDED || super.onNestedPreFling(
//            coordinatorLayout,
//            child,
//            target,
//            velocityX,
//            velocityY
//        ))
//    }
//
//    /**
//     * Gets the height of the bottom sheet when it is collapsed.
//     *
//     * @return The height of the collapsed bottom sheet in pixels, or [.PEEK_HEIGHT_AUTO]
//     * if the sheet is configured to peek automatically at 16:9 ratio keyline
//     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
//     */
//    /**
//     * Sets the height of the bottom sheet when it is collapsed.
//     *
//     * @param peekHeight The height of the collapsed bottom sheet in pixels, or
//     * [.PEEK_HEIGHT_AUTO] to configure the sheet to peek automatically
//     * at 16:9 ratio keyline.
//     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
//     */
//    var peekHeight: Int
//        get() = if (mPeekHeightAuto) PEEK_HEIGHT_AUTO else mPeekHeight
//        set(peekHeight) {
//            var layout = false
//            if (peekHeight == PEEK_HEIGHT_AUTO) {
//                if (!mPeekHeightAuto) {
//                    mPeekHeightAuto = true
//                    layout = true
//                }
//            } else if (mPeekHeightAuto || mPeekHeight != peekHeight) {
//                mPeekHeightAuto = false
//                mPeekHeight = Math.max(0, peekHeight)
//                mMaxOffset = mParentHeight - peekHeight
//                layout = true
//            }
//            if (layout && mState == STATE_COLLAPSED && mViewRef != null) {
//                val view = mViewRef.get()
//                view?.requestLayout()
//            }
//        }
//
//    /**
//     * Sets a callback to be notified of bottom sheet events.
//     *
//     * @param callback The callback to notify when bottom sheet events occur.
//     */
//    fun setBottomSheetCallback(callback: BottomSheetCallback?) {
//        mCallback = callback
//    }
//
//    /**
//     * Gets the current state of the bottom sheet.
//     *
//     * @return One of [.STATE_EXPANDED], [.STATE_HALF], [.STATE_COLLAPSED], [.STATE_DRAGGING],
//     * and [.STATE_SETTLING].
//     */// The view is not laid out yet; modify mState and let onLayoutChild handle it later
//    // Start the animation; wait until a pending layout if there is one.
//    /**
//     * Sets the state of the bottom sheet. The bottom sheet will transition to that state with
//     * animation.
//     *
//     * @param state One of [.STATE_COLLAPSED], [.STATE_EXPANDED], or [.STATE_HIDDEN], or(@link #STATE_HALF).
//     */
//    @get:State
//    var state: Int
//        get() = mState
//        set(state) {
//            if (state == mState) {
//                return
//            }
//            if (mViewRef == null) {
//                // The view is not laid out yet; modify mState and let onLayoutChild handle it later
//                if (state == STATE_COLLAPSED || state == STATE_EXPANDED || state == STATE_HALF || isHideable && state == STATE_HIDDEN) {
//                    mState = state
//                }
//                return
//            }
//            val child = mViewRef.get() ?: return
//            // Start the animation; wait until a pending layout if there is one.
//            val parent = child.parent
//            if (parent != null && parent.isLayoutRequested && ViewCompat.isAttachedToWindow(child)) {
//                child.post { startSettlingAnimation(child, state) }
//            } else {
//                startSettlingAnimation(child, state)
//            }
//        }
//
//    private fun setStateInternal(@com.riningan.widget.ExtendedBottomSheetBehavior.State state: Int) {
//        if (mState == state) {
//            return
//        }
//        mState = state
//        val bottomSheet: View? = mViewRef!!.get()
//        if (bottomSheet != null && mCallback != null) {
//            mCallback!!.onStateChanged(bottomSheet, state)
//        }
//    }
//
//    private fun reset() {
//        mActivePointerId = ViewDragHelper.INVALID_POINTER
//        if (mVelocityTracker != null) {
//            mVelocityTracker!!.recycle()
//            mVelocityTracker = null
//        }
//    }
//
//    private fun shouldHide(child: View, yvel: Float): Boolean {
//        if (skipCollapsed) {
//            return true
//        }
//        if (child.top < mMaxOffset) {
//            // It should not hide, but collapse.
//            return false
//        }
//        val newTop =
//            child.top + yvel * HIDE_FRICTION
//        return Math.abs(newTop - mMaxOffset) / mPeekHeight.toFloat() > HIDE_THRESHOLD
//    }
//
//    private fun findScrollingChild(view: View): View? {
//        if (view is NestedScrollingChild) {
//            return view
//        }
//        if (view is ViewGroup) {
//            val group = view
//            var i = 0
//            val count = group.childCount
//            while (i < count) {
//                val scrollingChild = findScrollingChild(group.getChildAt(i))
//                if (scrollingChild != null) {
//                    return scrollingChild
//                }
//                i++
//            }
//        }
//        return null
//    }
//
//    private val yVelocity: Float
//        private get() {
//            mVelocityTracker!!.computeCurrentVelocity(1000, mMaximumVelocity)
//            return VelocityTrackerCompat.getYVelocity(mVelocityTracker, mActivePointerId)
//        }
//
//    private fun startSettlingAnimation(child: View, state: Int) {
//        val top: Int
//        top = if (state == STATE_COLLAPSED) {
//            mMaxOffset
//        } else if (state == STATE_EXPANDED) {
//            mMinOffset
//        } else if (state == STATE_HALF) {
//            mHalfOffset
//        } else if (isHideable && state == STATE_HIDDEN) {
//            mParentHeight
//        } else {
//            throw IllegalArgumentException("Illegal state argument: $state")
//        }
//        setStateInternal(STATE_SETTLING)
//        if (mViewDragHelper.smoothSlideViewTo(child, child.left, top)) {
//            ViewCompat.postOnAnimation(
//                child,
//                com.riningan.widget.ExtendedBottomSheetBehavior.SettleRunnable(child, state)
//            )
//        }
//    }
//
//    private val mDragCallback: ViewDragHelper.Callback = object : Callback() {
//        fun tryCaptureView(@NonNull child: View, pointerId: Int): Boolean {
//            if (mState == STATE_DRAGGING) {
//                return false
//            }
//            if (mTouchingScrollingChild) {
//                return false
//            }
//            if (mState == STATE_EXPANDED && mActivePointerId == pointerId) {
//                val scroll = mNestedScrollingChildRef!!.get()
//                if (scroll != null && ViewCompat.canScrollVertically(scroll, -1)) {
//                    // Let the content scroll up
//                    return false
//                }
//            }
//            return mViewRef != null && mViewRef.get() === child
//        }
//
//        fun onViewPositionChanged(
//            @NonNull changedView: View?,
//            left: Int,
//            top: Int,
//            dx: Int,
//            dy: Int
//        ) {
//            dispatchOnSlide(top)
//        }
//
//        fun onViewDragStateChanged(state: Int) {
//            if (state == ViewDragHelper.STATE_DRAGGING) {
//                setStateInternal(STATE_DRAGGING)
//            }
//        }
//
//        fun onViewReleased(
//            @NonNull releasedChild: View,
//            xvel: Float,
//            yvel: Float
//        ) {
//            val top: Int
//            @com.riningan.widget.ExtendedBottomSheetBehavior.State val targetState: Int
//            if (yvel < 0) { // Moving up
//                top = mMinOffset
//                targetState = STATE_EXPANDED
//            } else if (isHideable && shouldHide(releasedChild, yvel)) {
//                top = mParentHeight
//                targetState = STATE_HIDDEN
//            } else if (yvel == 0f) {
//                val currentTop = releasedChild.top
//                if (Math.abs(currentTop - mMinOffset) < Math.abs(currentTop - mMaxOffset)) {
//                    top = mMinOffset
//                    targetState = STATE_EXPANDED
//                } else {
//                    top = mMaxOffset
//                    targetState = STATE_COLLAPSED
//                }
//            } else {
//                top = mMaxOffset
//                targetState = STATE_COLLAPSED
//            }
//            if (mViewDragHelper.settleCapturedViewAt(releasedChild.left, top)) {
//                setStateInternal(STATE_SETTLING)
//                ViewCompat.postOnAnimation(
//                    releasedChild,
//                    com.riningan.widget.ExtendedBottomSheetBehavior.SettleRunnable(
//                        releasedChild,
//                        targetState
//                    )
//                )
//            } else {
//                setStateInternal(targetState)
//            }
//        }
//
//        fun clampViewPositionVertical(@NonNull child: View?, top: Int, dy: Int): Int {
//            val high = if (isHideable) mParentHeight else mMaxOffset
//            return if (top < mMinOffset) mMinOffset else if (top > high) high else top
//        }
//
//        fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
//            return child.left
//        }
//
//        fun getViewVerticalDragRange(@NonNull child: View?): Int {
//            return if (isHideable) {
//                mParentHeight - mMinOffset
//            } else {
//                mMaxOffset - mMinOffset
//            }
//        }
//    }
//
//    private fun dispatchOnSlide(top: Int) {
//        val bottomSheet: View? = mViewRef!!.get()
//        if (bottomSheet != null && mCallback != null) {
//            if (top > mMaxOffset) {
//                mCallback!!.onSlide(
//                    bottomSheet,
//                    (mMaxOffset - top).toFloat() / (mParentHeight - mMaxOffset)
//                )
//            } else {
//                mCallback!!.onSlide(
//                    bottomSheet,
//                    (mMaxOffset - top).toFloat() / (mMaxOffset - mMinOffset)
//                )
//            }
//        }
//    }
//
//    private inner class SettleRunnable internal constructor(
//        private val mView: View,
//        @field:State @param:State private val mTargetState: Int
//    ) :
//        Runnable {
//
//        override fun run() {
//            if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
//                ViewCompat.postOnAnimation(mView, this)
//            } else {
//                setStateInternal(mTargetState)
//            }
//        }
//
//    }
//
//    private class SavedState : AbsSavedState {
//        @com.riningan.widget.ExtendedBottomSheetBehavior.State
//        val state: Int
//
//        constructor(source: Parcel) : this(source, null) {}
//        private constructor(source: Parcel, loader: ClassLoader?) : super(
//            source,
//            loader
//        ) {
//            state = source.readInt()
//        }
//
//        private constructor(
//            superState: Parcelable,
//            @com.riningan.widget.ExtendedBottomSheetBehavior.State state: Int
//        ) : super(superState) {
//            this.state = state
//        }
//
//        override fun writeToParcel(out: Parcel, flags: Int) {
//            super.writeToParcel(out, flags)
//            out.writeInt(state)
//        }
//
//        companion object {
//            val CREATOR: Creator<com.riningan.widget.ExtendedBottomSheetBehavior.SavedState> =
//                ParcelableCompat.newCreator(
//                    object :
//                        ParcelableCompatCreatorCallbacks<com.riningan.widget.ExtendedBottomSheetBehavior.SavedState?>() {
//                        fun createFromParcel(
//                            `in`: Parcel?,
//                            loader: ClassLoader?
//                        ): com.riningan.widget.ExtendedBottomSheetBehavior.SavedState {
//                            return com.riningan.widget.ExtendedBottomSheetBehavior.SavedState(
//                                `in`,
//                                loader
//                            )
//                        }
//
//                        fun newArray(size: Int): Array<com.riningan.widget.ExtendedBottomSheetBehavior.SavedState?> {
//                            return arrayOfNulls<com.riningan.widget.ExtendedBottomSheetBehavior.SavedState>(
//                                size
//                            )
//                        }
//                    })
//        }
//    }
//
//    companion object {
//        /**
//         * The bottom sheet is dragging.
//         */
//        const val STATE_DRAGGING = 1
//
//        /**
//         * The bottom sheet is settling.
//         */
//        const val STATE_SETTLING = 2
//
//        /**
//         * The bottom sheet is expanded.
//         */
//        const val STATE_EXPANDED = 3
//
//        /**
//         * The bottom sheet is collapsed.
//         */
//        const val STATE_COLLAPSED = 4
//
//        /**
//         * The bottom sheet is hidden.
//         */
//        const val STATE_HIDDEN = 5
//
//        /**
//         * The bottom sheet is expanded.
//         */
//        const val STATE_HALF = 6
//
//        /**
//         * Peek at the 16:9 ratio keyline of its parent.
//         *
//         *
//         *
//         * This can be used as a parameter for [.setPeekHeight].
//         * [.getPeekHeight] will return this when the value is set.
//         */
//        const val PEEK_HEIGHT_AUTO = -1
//        private const val HIDE_THRESHOLD = 0.5f
//        private const val HIDE_FRICTION = 0.1f
//
//        /**
//         * A utility function to get the [ExtendedBottomSheetBehavior] associated with the `view`.
//         *
//         * @param view The [View] with [ExtendedBottomSheetBehavior].
//         * @return The [ExtendedBottomSheetBehavior] associated with the `view`.
//         */
//        fun <V : View?> from(view: V): ExtendedBottomSheetBehavior<V> {
//            val params = view!!.layoutParams
//            require(params is CoordinatorLayout.LayoutParams) { "The view is not a child of CoordinatorLayout" }
//            val behavior: CoordinatorLayout.Behavior =
//                (params as CoordinatorLayout.LayoutParams).getBehavior()
//            require(behavior is ExtendedBottomSheetBehavior<*>) { "The view is not associated with BottomSheetBehavior" }
//            return behavior
//        }
//    }
//}