package com.yikwing.component

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.yikwing.extension.common.STATUS_BAR_HEIGHT
import com.yikwing.extension.common.dp

class ImBarWrapperView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        init {
            setPadding(0, STATUS_BAR_HEIGHT + 10.dp, 0, 0)
        }
    }
