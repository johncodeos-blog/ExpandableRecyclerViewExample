package com.example.expandablerecyclerviewexample

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout

/**
 * Created by SilenceDut.
 */
class ExpandableLayout : LinearLayout {
    private val PREINIT = -1
    private val CLOSED = 0
    private val EXPANDED = 1
    private val EXPANDING = 2
    private val CLOSING = 3
    var mExpandState = 0
    private var mExpandAnimator: ValueAnimator? = null
    private var mParentAnimator: ValueAnimator? = null
    private var mExpandedViewHeight = 0
    private var sIsInit = true
    private var mExpandDuration = EXPAND_DURATION
    private var mExpandWithParentScroll = false
    private var mExpandScrollTogether = false
    private var mOnExpandListener: OnExpandListener? = null

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private fun init(attrs: AttributeSet?) {
        isClickable = true
        orientation = VERTICAL
        this.clipChildren = false
        this.clipToPadding = false
        mExpandState = PREINIT
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableLayout)
            mExpandDuration = typedArray.getInt(R.styleable.ExpandableLayout_expDuration, EXPAND_DURATION)
            mExpandWithParentScroll = typedArray.getBoolean(R.styleable.ExpandableLayout_expWithParentScroll, false)
            mExpandScrollTogether = typedArray.getBoolean(R.styleable.ExpandableLayout_expExpandScrollTogether, false)
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val childCount = childCount
        check(childCount == 2) { "ExpandableLayout must has two child view !" }
        if (sIsInit) {
            (getChildAt(0).layoutParams as MarginLayoutParams).bottomMargin = 0
            val marginLayoutParams = getChildAt(1).layoutParams as MarginLayoutParams
            marginLayoutParams.bottomMargin = 0
            marginLayoutParams.topMargin = 0
            marginLayoutParams.height = 0
            mExpandedViewHeight = getChildAt(1).measuredHeight
            sIsInit = false
            mExpandState = CLOSED
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun verticalAnimate(startHeight: Int, endHeight: Int) {
        val mViewParent = parent as ViewGroup
        val distance = (y + measuredHeight + mExpandedViewHeight - mViewParent.measuredHeight).toInt()
        val target = getChildAt(1)
        mExpandAnimator = ValueAnimator.ofInt(startHeight, endHeight)
        mExpandAnimator?.addUpdateListener(AnimatorUpdateListener { animation ->
            target.layoutParams.height = animation.animatedValue as Int
            target.requestLayout()
        })
        mExpandAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (endHeight - startHeight < 0) {
                    mExpandState = CLOSED
                    if (mOnExpandListener != null) {
                        mOnExpandListener!!.onExpand(false)
                    }
                } else {
                    mExpandState = EXPANDED
                    if (mOnExpandListener != null) {
                        mOnExpandListener!!.onExpand(true)
                    }
                }
            }
        })
        mExpandState = if (mExpandState == EXPANDED) CLOSING else EXPANDING
        mExpandAnimator?.duration = mExpandDuration.toLong()
        if (mExpandState == EXPANDING && mExpandWithParentScroll && distance > 0) {
            mExpandAnimator = parentScroll(distance)
            val animatorSet = AnimatorSet()
            if (mExpandScrollTogether) {
                animatorSet.playSequentially(mExpandAnimator, mParentAnimator)
            } else {
                animatorSet.playTogether(mExpandAnimator, mParentAnimator)
            }
            animatorSet.start()
        } else {
            mExpandAnimator?.start()
        }
    }

    private fun parentScroll(distance: Int): ValueAnimator? {
        val mViewParent = parent as ViewGroup
        mParentAnimator = ValueAnimator.ofInt(0, distance)
        mParentAnimator?.addUpdateListener(object : AnimatorUpdateListener {
            var lastDy = 0
            var dy = 0
            override fun onAnimationUpdate(animation: ValueAnimator) {
                dy = animation.animatedValue as Int - lastDy
                lastDy = animation.animatedValue as Int
                mViewParent.scrollBy(0, dy)
            }
        })
        mParentAnimator?.duration = mExpandDuration.toLong()
        return mExpandAnimator
    }

    fun setExpand(expand: Boolean) {
        if (mExpandState == PREINIT) {
            return
        }
        getChildAt(1).layoutParams.height = if (expand) mExpandedViewHeight else 0
        requestLayout()
        mExpandState = if (expand) EXPANDED else CLOSED
    }

    val isExpanded: Boolean
        get() = mExpandState == EXPANDED

    private fun toggle() {
        if (mExpandState == EXPANDED) {
            close()
        } else if (mExpandState == CLOSED) {
            expand()
        }
    }

    private fun expand() {
        verticalAnimate(0, mExpandedViewHeight)
    }

    private fun close() {
        verticalAnimate(mExpandedViewHeight, 0)
    }

    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }

    interface OnExpandListener {
        fun onExpand(expanded: Boolean)
    }

    fun setOnExpandListener(onExpandListener: OnExpandListener?) {
        mOnExpandListener = onExpandListener
    }

    fun setExpandScrollTogether(expandScrollTogether: Boolean) {
        mExpandScrollTogether = expandScrollTogether
    }

    fun setExpandWithParentScroll(expandWithParentScroll: Boolean) {
        mExpandWithParentScroll = expandWithParentScroll
    }

    fun setExpandDuration(expandDuration: Int) {
        mExpandDuration = expandDuration
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mExpandAnimator != null && mExpandAnimator!!.isRunning) {
            mExpandAnimator!!.cancel()
        }
        if (mParentAnimator != null && mParentAnimator!!.isRunning) {
            mParentAnimator!!.cancel()
        }
    }

    companion object {
        private val TAG = ExpandableLayout::class.java.simpleName
        private const val EXPAND_DURATION = 300
    }
}