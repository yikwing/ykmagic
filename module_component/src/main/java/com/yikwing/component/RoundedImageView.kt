package com.yikwing.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.yikwing.module_view.R

/**
 * 圆角 ImageView
 * cornerRadius 圆角半径
 * isCircle 是否显示为圆形
 * */
class RoundedImageView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : AppCompatImageView(context, attrs, defStyleAttr) {
        private var radius: Float = 0f // 圆角半径
        private var isCircle: Boolean = false // 是否显示为圆形
        private val path = Path()
        private val rect = RectF()

        init {
            context.theme
                .obtainStyledAttributes(
                    attrs,
                    R.styleable.RoundedImageView,
                    0,
                    0,
                ).apply {
                    try {
                        radius = getDimension(R.styleable.RoundedImageView_cornerRadius, 0f)
                        isCircle = getBoolean(R.styleable.RoundedImageView_isCircle, false)
                    } finally {
                        recycle()
                    }
                }
        }

        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int,
        ) {
            super.onSizeChanged(w, h, oldw, oldh)
            rect.left = 0f
            rect.top = 0f
            rect.right = w.toFloat()
            rect.bottom = h.toFloat()
        }

        override fun onDraw(canvas: Canvas) {
            if (isCircle) {
                val diameter = width.coerceAtMost(height)
                radius = diameter / 2f
            }
            path.reset()
            path.addRoundRect(rect, radius, radius, Path.Direction.CW)
            canvas.clipPath(path)
            super.onDraw(canvas)
        }
    }
