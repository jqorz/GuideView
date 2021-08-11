package com.jqorz.guideview

import android.view.LayoutInflater
import android.view.View

/**
 * * 遮罩系统中相对于目标区域而绘制一些图片或者文字等view需要实现的接口. <br></br>
 * * <br></br>
 * * [.getView] <br></br>
 * * [.getAnchor] <br></br>
 * * [.getFitPosition] <br></br>
 * * [.getXOffset] <br></br>
 * * [.getYOffset]
 * * <br></br>
 * * 具体创建遮罩的说明请参加[GuideBuilder]
 * *
 *
 *
 * Created by binIoter
 */
interface Component {
    /**
     * 需要显示的view
     *
     * @param inflater use to inflate xml resource file
     * @return the component view
     */
    fun getView(inflater: LayoutInflater): View

    /**
     * 相对目标View的锚点
     *
     * @return could be [.ANCHOR_LEFT], [.ANCHOR_RIGHT],
     * [.ANCHOR_TOP], [.ANCHOR_BOTTOM], [.ANCHOR_OVER]
     */
    val anchor: Int

    /**
     * 相对目标View的对齐
     *
     * @return could be [.FIT_START], [.FIT_END],
     * [.FIT_CENTER]
     */
    val fitPosition: Int

    /**
     * 相对目标View的X轴位移，在计算锚点和对齐之后。
     *
     * @return X轴偏移量, 单位 dp
     */
    val xOffset: Int

    /**
     * 相对目标View的Y轴位移，在计算锚点和对齐之后。
     *
     * @return Y轴偏移量，单位 dp
     */
    val yOffset: Int

    companion object {
        val FIT_END: Int = MaskView.LayoutParams.PARENT_END
        val FIT_CENTER: Int = MaskView.LayoutParams.PARENT_CENTER
        val ANCHOR_LEFT: Int = MaskView.LayoutParams.ANCHOR_LEFT
        val ANCHOR_RIGHT: Int = MaskView.LayoutParams.ANCHOR_RIGHT
        val ANCHOR_BOTTOM: Int = MaskView.LayoutParams.ANCHOR_BOTTOM
        val ANCHOR_TOP: Int = MaskView.LayoutParams.ANCHOR_TOP
        val ANCHOR_OVER: Int = MaskView.LayoutParams.ANCHOR_OVER

        /**
         * 圆角矩形&矩形
         */
        const val ROUNDRECT = 0

        /**
         * 圆形
         */
        const val CIRCLE = 1
        val FIT_START: Int = MaskView.LayoutParams.PARENT_START
    }
}