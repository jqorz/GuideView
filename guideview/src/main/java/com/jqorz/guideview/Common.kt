package com.jqorz.guideview

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by binIoter
 */
internal object Common {
    /**
     * 设置Component
     */
    fun componentToView(inflater: LayoutInflater, c: Component): View {
        val view = c.getView(inflater)
        val lp = MaskView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            offsetX = c.xOffset
            offsetY = c.yOffset
            targetAnchor = c.anchor
            targetParentPosition = c.fitPosition
        }
        view.layoutParams?.let {
            lp.width = it.width
            lp.height = it.height
        }
        view.layoutParams = lp
        return view
    }

    /**
     * Rect在屏幕上去掉状态栏高度的绝对位置
     */
    fun getViewAbsRect(view: View?, parentX: Int, parentY: Int): Rect {
        val loc = IntArray(2)
        view!!.getLocationInWindow(loc)
        val rect = Rect()
        rect[loc[0], loc[1], loc[0] + view.measuredWidth] = loc[1] + view.measuredHeight
        rect.offset(-parentX, -parentY)
        return rect
    }
}