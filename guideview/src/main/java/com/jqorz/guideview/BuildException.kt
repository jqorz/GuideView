package com.jqorz.guideview

/**
 * 遮罩系统运行异常的封装
 * Created by binIoter
 */
internal class BuildException(private val mDetailMessage: String) : RuntimeException() {

    override val message: String
        get() = "Build GuideFragment failed: $mDetailMessage"
}