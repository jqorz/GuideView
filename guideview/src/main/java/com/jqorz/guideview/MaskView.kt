package com.jqorz.guideview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*

/**
 * Created by binIoter
 */
internal class MaskView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) :
    ViewGroup(context, attrs, defStyle) {
    /**
     * 高亮区域
     */
    private val mTargetRect = RectF()

    /**
     * 蒙层区域
     */
    private val mOverlayRect = RectF()

    /**
     * 中间变量
     */
    private val mChildTmpRect = RectF()

    /**
     * 蒙层背景画笔
     */
    private val mFullingPaint: Paint
    private var mPadding = 0
    private var mPaddingLeft = 0
    private var mPaddingTop = 0
    private var mPaddingRight = 0
    private var mPaddingBottom = 0

    /**
     * 是否覆盖目标区域
     */
    private var mOverlayTarget = false

    /**
     * 圆角大小
     */
    private var mCorner = 0

    /**
     * 目标区域样式，默认为矩形
     */
    private var mStyle: Int = Component.ROUNDRECT

    /**
     * 挖空画笔
     */
    private val mEraser: Paint

    /**
     * 橡皮擦Bitmap
     */
    private var mEraserBitmap: Bitmap?

    /**
     * 橡皮擦Cavas
     */
    private val mEraserCanvas: Canvas
    private var ignoreRepadding = false
    private var mInitHeight = 0
    private var mChangedHeight = 0
    private var mFirstFlag = true
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        try {
            clearFocus()
            mEraserCanvas.setBitmap(null)
            mEraserBitmap = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        if (mFirstFlag) {
            mInitHeight = h
            mFirstFlag = false
        }
        mChangedHeight = if (mInitHeight > h) {
            h - mInitHeight
        } else if (mInitHeight < h) {
            h - mInitHeight
        } else {
            0
        }
        setMeasuredDimension(w, h)
        mOverlayRect[0f, 0f, w.toFloat()] = h.toFloat()
        resetOutPath()
        val count = childCount
        var child: View?
        for (i in 0 until count) {
            child = getChildAt(i)
            child?.let { measureChild(it, widthMeasureSpec, heightMeasureSpec) }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val density = resources.displayMetrics.density
        var child: View?
        for (i in 0 until count) {
            child = getChildAt(i)
            if (child == null) {
                continue
            }
            val lp = child.layoutParams as LayoutParams
            when (lp.targetAnchor) {
                LayoutParams.ANCHOR_LEFT -> {
                    mChildTmpRect.right = mTargetRect.left
                    mChildTmpRect.left = mChildTmpRect.right - child.measuredWidth
                    verticalChildPositionLayout(child, mChildTmpRect, lp.targetParentPosition)
                }
                LayoutParams.ANCHOR_TOP -> {
                    mChildTmpRect.bottom = mTargetRect.top
                    mChildTmpRect.top = mChildTmpRect.bottom - child.measuredHeight
                    horizontalChildPositionLayout(child, mChildTmpRect, lp.targetParentPosition)
                }
                LayoutParams.ANCHOR_RIGHT -> {
                    mChildTmpRect.left = mTargetRect.right
                    mChildTmpRect.right = mChildTmpRect.left + child.measuredWidth
                    verticalChildPositionLayout(child, mChildTmpRect, lp.targetParentPosition)
                }
                LayoutParams.ANCHOR_BOTTOM -> {
                    mChildTmpRect.top = mTargetRect.bottom
                    mChildTmpRect.bottom = mChildTmpRect.top + child.measuredHeight
                    horizontalChildPositionLayout(child, mChildTmpRect, lp.targetParentPosition)
                }
                LayoutParams.ANCHOR_OVER -> {
                    mChildTmpRect.left = (mTargetRect.width().toInt() - child.measuredWidth shr 1).toFloat()
                    mChildTmpRect.top = (mTargetRect.height().toInt() - child.measuredHeight shr 1).toFloat()
                    mChildTmpRect.right = (mTargetRect.width().toInt() + child.measuredWidth shr 1).toFloat()
                    mChildTmpRect.bottom = (mTargetRect.height().toInt() + child.measuredHeight shr 1).toFloat()
                    mChildTmpRect.offset(mTargetRect.left, mTargetRect.top)
                }
            }
            //额外的xy偏移
            mChildTmpRect.offset(
                (density * lp.offsetX + 0.5f),
                (density * lp.offsetY + 0.5f)
            )
            child.layout(
                mChildTmpRect.left.toInt(), mChildTmpRect.top.toInt(), mChildTmpRect.right.toInt(),
                mChildTmpRect.bottom.toInt()
            )
        }
    }

    private fun horizontalChildPositionLayout(child: View, rect: RectF, targetParentPosition: Int) {
        when (targetParentPosition) {
            LayoutParams.PARENT_START -> {
                rect.left = mTargetRect.left
                rect.right = rect.left + child.measuredWidth
            }
            LayoutParams.PARENT_CENTER -> {
                rect.left = (mTargetRect.width() - child.measuredWidth) / 2
                rect.right = (mTargetRect.width() + child.measuredWidth) / 2
                rect.offset(mTargetRect.left, 0f)
            }
            LayoutParams.PARENT_END -> {
                rect.right = mTargetRect.right
                rect.left = rect.right - child.measuredWidth
            }
        }
    }

    private fun verticalChildPositionLayout(child: View, rect: RectF, targetParentPosition: Int) {
        when (targetParentPosition) {
            LayoutParams.PARENT_START -> {
                rect.top = mTargetRect.top
                rect.bottom = rect.top + child.measuredHeight
            }
            LayoutParams.PARENT_CENTER -> {
                rect.top = (mTargetRect.width() - child.measuredHeight) / 2
                rect.bottom = (mTargetRect.width() + child.measuredHeight) / 2
                rect.offset(0f, mTargetRect.top)
            }
            LayoutParams.PARENT_END -> {
                rect.bottom = mTargetRect.bottom
                rect.top = mTargetRect.bottom - child.measuredHeight
            }
        }
    }

    private fun resetOutPath() {
        resetPadding()
    }

    /**
     * 设置padding
     */
    private fun resetPadding() {
        if (!ignoreRepadding) {
            if (mPadding != 0 && mPaddingLeft == 0) {
                mTargetRect.left -= mPadding.toFloat()
            }
            if (mPadding != 0 && mPaddingTop == 0) {
                mTargetRect.top -= mPadding.toFloat()
            }
            if (mPadding != 0 && mPaddingRight == 0) {
                mTargetRect.right += mPadding.toFloat()
            }
            if (mPadding != 0 && mPaddingBottom == 0) {
                mTargetRect.bottom += mPadding.toFloat()
            }
            if (mPaddingLeft != 0) {
                mTargetRect.left -= mPaddingLeft.toFloat()
            }
            if (mPaddingTop != 0) {
                mTargetRect.top -= mPaddingTop.toFloat()
            }
            if (mPaddingRight != 0) {
                mTargetRect.right += mPaddingRight.toFloat()
            }
            if (mPaddingBottom != 0) {
                mTargetRect.bottom += mPaddingBottom.toFloat()
            }
            ignoreRepadding = true
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val drawingTime = drawingTime
        try {
            var child: View?
            for (i in 0 until childCount) {
                child = getChildAt(i)
                drawChild(canvas, child, drawingTime)
            }
        } catch (e: NullPointerException) {
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mChangedHeight != 0) {
            mTargetRect.offset(0f, mChangedHeight.toFloat())
            mInitHeight += mChangedHeight
            mChangedHeight = 0
        }
        mEraserBitmap!!.eraseColor(Color.TRANSPARENT)
        mEraserCanvas.drawColor(mFullingPaint.color)
        if (!mOverlayTarget) {
            when (mStyle) {
                Component.ROUNDRECT -> mEraserCanvas.drawRoundRect(
                    mTargetRect,
                    mCorner.toFloat(),
                    mCorner.toFloat(),
                    mEraser
                )
                Component.CIRCLE -> mEraserCanvas.drawCircle(
                    mTargetRect.centerX(), mTargetRect.centerY(),
                    mTargetRect.width() / 2, mEraser
                )
                else -> mEraserCanvas.drawRoundRect(mTargetRect, mCorner.toFloat(), mCorner.toFloat(), mEraser)
            }
        }
        canvas.drawBitmap(mEraserBitmap!!, mOverlayRect.left, mOverlayRect.top, null)
    }

    fun setTargetRect(rect: Rect?) {
        mTargetRect.set(rect)
    }

    fun setFullingAlpha(alpha: Int) {
        mFullingPaint.alpha = alpha
    }

    fun setFullingColor(color: Int) {
        mFullingPaint.color = color
    }

    fun setHighTargetCorner(corner: Int) {
        mCorner = corner
    }

    fun setHighTargetGraphStyle(style: Int) {
        mStyle = style
    }

    fun setOverlayTarget(b: Boolean) {
        mOverlayTarget = b
    }

    fun setPadding(padding: Int) {
        mPadding = padding
    }

    fun setPaddingLeft(paddingLeft: Int) {
        mPaddingLeft = paddingLeft
    }

    fun setPaddingTop(paddingTop: Int) {
        mPaddingTop = paddingTop
    }

    fun setPaddingRight(paddingRight: Int) {
        mPaddingRight = paddingRight
    }

    fun setPaddingBottom(paddingBottom: Int) {
        mPaddingBottom = paddingBottom
    }

    internal class LayoutParams(width: Int, height: Int) : ViewGroup.LayoutParams(width, height) {
        var targetAnchor = ANCHOR_BOTTOM
        var targetParentPosition = PARENT_CENTER
        var offsetX = 0
        var offsetY = 0

        companion object {
            const val ANCHOR_LEFT = 0x01
            const val ANCHOR_TOP = 0x02
            const val ANCHOR_RIGHT = 0x03
            const val ANCHOR_BOTTOM = 0x04
            const val ANCHOR_OVER = 0x05
            const val PARENT_START = 0x10
            const val PARENT_CENTER = 0x20
            const val PARENT_END = 0x30
        }
    }

    init {
        //自我绘制
        setWillNotDraw(false)
        val wm = getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        wm.defaultDisplay.getRealMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        mOverlayRect[0f, 0f, width.toFloat()] = height.toFloat()
        mEraserBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mEraserCanvas = Canvas(mEraserBitmap)
        mFullingPaint = Paint()
        mEraser = Paint()
        mEraser.color = -0x1
        //图形重叠时的处理方式，擦除效果
        mEraser.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        //位图抗锯齿设置
        mEraser.flags = Paint.ANTI_ALIAS_FLAG
    }
}