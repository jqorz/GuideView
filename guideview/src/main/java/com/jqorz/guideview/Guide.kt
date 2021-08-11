package com.jqorz.guideview

import android.app.Activity
import android.view.*
import android.view.View.OnTouchListener
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import com.jqorz.guideview.GuideBuilder.*

/**
 * 遮罩系统的封装 <br></br>
 * * 外部需要调用[GuideBuilder]来创建该实例，实例创建后调用
 * * [.show] 控制显示； 调用 [.dismiss]让遮罩系统消失。 <br></br>
 *
 *
 * Created by binIoter
 */
class Guide internal constructor() : View.OnKeyListener, OnTouchListener {
    private var mConfiguration: Configuration? = null
    private var mMaskView: MaskView? = null
    private var mComponents: Array<Component>? = null

    // 根据locInwindow定位后，是否需要判断loc值非0
    private var mShouldCheckLocInWindow = true
    private var mOnVisibilityChangedListener: OnVisibilityChangedListener? = null
    private var mOnSlideListener: OnSlideListener? = null
    fun setConfiguration(configuration: Configuration?) {
        mConfiguration = configuration
    }

    fun setComponents(components: Array<Component>?) {
        mComponents = components
    }

    fun setCallback(listener: OnVisibilityChangedListener?) {
        mOnVisibilityChangedListener = listener
    }

    fun setOnSlideListener(onSlideListener: OnSlideListener?) {
        mOnSlideListener = onSlideListener
    }
    /**
     * 显示遮罩
     *
     * @param activity 目标Activity
     * @param overlay0  遮罩层view
     */
    /**
     * 显示遮罩
     *
     * @param activity 目标Activity
     */
    @JvmOverloads
    fun show(activity: Activity, overlay0: ViewGroup? = null) {
        var overlay = overlay0
        mMaskView = onCreateView(activity, overlay)
        if (overlay == null) {
            overlay = activity.window.decorView as ViewGroup
        }
        if (mMaskView!!.parent == null && mConfiguration!!.mTargetView != null) {
            overlay.addView(mMaskView)
            if (mConfiguration!!.mEnterAnimationId != -1) {
                val anim = AnimationUtils.loadAnimation(activity, mConfiguration!!.mEnterAnimationId)!!
                anim.setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        if (mOnVisibilityChangedListener != null) {
                            mOnVisibilityChangedListener!!.onShown()
                        }
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                mMaskView!!.startAnimation(anim)
            } else {
                if (mOnVisibilityChangedListener != null) {
                    mOnVisibilityChangedListener!!.onShown()
                }
            }
        }
    }

    fun clear() {
        if (mMaskView == null) {
            return
        }
        val vp = mMaskView!!.parent as ViewGroup ?: return
        vp.removeView(mMaskView)
        onDestroy()
    }

    /**
     * 隐藏该遮罩并回收资源相关
     */
    fun dismiss() {
        if (mMaskView == null) {
            return
        }
        val vp = mMaskView!!.parent as ViewGroup ?: return
        if (mConfiguration!!.mExitAnimationId != -1) {
            // mMaskView may leak if context is null
            val context = mMaskView!!.context!!
            val anim = AnimationUtils.loadAnimation(context, mConfiguration!!.mExitAnimationId)!!
            anim.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    vp.removeView(mMaskView)
                    if (mOnVisibilityChangedListener != null) {
                        mOnVisibilityChangedListener!!.onDismiss()
                    }
                    onDestroy()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            mMaskView!!.startAnimation(anim)
        } else {
            vp.removeView(mMaskView)
            if (mOnVisibilityChangedListener != null) {
                mOnVisibilityChangedListener!!.onDismiss()
            }
            onDestroy()
        }
    }

    /**
     * 根据locInwindow定位后，是否需要判断loc值非0
     */
    fun setShouldCheckLocInWindow(set: Boolean) {
        mShouldCheckLocInWindow = set
    }

    private fun onCreateView(activity: Activity, overlay: ViewGroup?): MaskView {
        var overlay = overlay
        if (overlay == null) {
            overlay = activity.window.decorView as ViewGroup
        }
        val maskView = MaskView(activity)
        maskView.setFullingColor(activity.resources.getColor(mConfiguration!!.mFullingColorId))
        maskView.setFullingAlpha(mConfiguration!!.mAlpha)
        maskView.setHighTargetCorner(mConfiguration!!.mCorner)
        maskView.setPadding(mConfiguration!!.mPadding)
        maskView.paddingLeft = mConfiguration!!.mPaddingLeft
        maskView.paddingTop = mConfiguration!!.mPaddingTop
        maskView.paddingRight = mConfiguration!!.mPaddingRight
        maskView.paddingBottom = mConfiguration!!.mPaddingBottom
        maskView.setHighTargetGraphStyle(mConfiguration!!.mGraphStyle)
        maskView.setOverlayTarget(mConfiguration!!.mOverlayTarget)
        maskView.setOnKeyListener(this)

        // For removing the height of status bar we need the root content view's
        // location on screen
        var parentX = 0
        var parentY = 0
        if (overlay != null) {
            val loc = IntArray(2)
            overlay.getLocationInWindow(loc)
            parentX = loc[0]
            parentY = loc[1]
        }
        if (mConfiguration!!.mTargetView != null) {
            maskView.setTargetRect(Common.getViewAbsRect(mConfiguration!!.mTargetView, parentX, parentY))
        } else {
            // Gets the target view's abs rect
            val target = activity.findViewById<View>(mConfiguration!!.mTargetViewId)
            if (target != null) {
                maskView.setTargetRect(Common.getViewAbsRect(target, parentX, parentY))
            }
        }
        if (mConfiguration!!.mOutsideTouchable) {
            maskView.isClickable = false
        } else {
            maskView.setOnTouchListener(this)
        }

        // Adds the components to the mask view.
        for (c in mComponents!!) {
            maskView.addView(Common.componentToView(activity.layoutInflater, c))
        }
        return maskView
    }

    private fun onDestroy() {
        mConfiguration = null
        mComponents = null
        mOnVisibilityChangedListener = null
        mOnSlideListener = null
        mMaskView!!.removeAllViews()
        mMaskView = null
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            if (mConfiguration != null && mConfiguration!!.mAutoDismiss) {
                dismiss()
                true
            } else {
                false
            }
        } else false
    }

    var startY = -1f
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            startY = motionEvent.y
        } else if (motionEvent.action == MotionEvent.ACTION_UP) {
            if (startY - motionEvent.y > DimenUtil.dp2px(view.context, SLIDE_THRESHOLD.toFloat())) {
                if (mOnSlideListener != null) {
                    mOnSlideListener!!.onSlideListener(SlideState.UP)
                }
            } else if (motionEvent.y - startY > DimenUtil.dp2px(view.context, SLIDE_THRESHOLD.toFloat())) {
                if (mOnSlideListener != null) {
                    mOnSlideListener!!.onSlideListener(SlideState.DOWN)
                }
            }
            if (mConfiguration != null && mConfiguration!!.mAutoDismiss) {
                dismiss()
            }
        }
        return true
    }

    companion object {
        /**
         * 滑动临界值
         */
        private const val SLIDE_THRESHOLD = 30
    }
}