package com.yikwing.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * 圆角 ImageView
 * cornerRadius 圆角半径
 * topLeftRadius 左上角圆角半径
 * topRightRadius 右上角圆角半径
 * bottomLeftRadius 左下角圆角半径
 * bottomRightRadius 右下角圆角半径
 * isCircle 是否显示为圆形
 * */
class RoundedImageView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : AppCompatImageView(context, attrs, defStyleAttr) {
        private var cornerRadius: Float = 0f // 统一圆角半径
        private var topLeftRadius: Float = 0f // 左上角圆角半径
        private var topRightRadius: Float = 0f // 右上角圆角半径
        private var bottomLeftRadius: Float = 0f // 左下角圆角半径
        private var bottomRightRadius: Float = 0f // 右下角圆角半径
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
                        cornerRadius = getDimension(R.styleable.RoundedImageView_cornerRadius, 0f)
                        topLeftRadius =
                            getDimension(R.styleable.RoundedImageView_topLeftRadius, cornerRadius)
                        topRightRadius =
                            getDimension(R.styleable.RoundedImageView_topRightRadius, cornerRadius)
                        bottomLeftRadius =
                            getDimension(R.styleable.RoundedImageView_bottomLeftRadius, cornerRadius)
                        bottomRightRadius =
                            getDimension(R.styleable.RoundedImageView_bottomRightRadius, cornerRadius)
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
                val circleRadius = diameter / 2f
                path.reset()
                path.addCircle(rect.centerX(), rect.centerY(), circleRadius, Path.Direction.CW)
            } else {
                val cornerRadii =
                    floatArrayOf(
                        topLeftRadius,
                        topLeftRadius, // 左上角 (x,y)
                        topRightRadius,
                        topRightRadius, // 右上角 (x,y)
                        bottomRightRadius,
                        bottomRightRadius, // 右下角 (x,y)
                        bottomLeftRadius,
                        bottomLeftRadius, // 左下角 (x,y)
                    )
                path.reset()
                path.addRoundRect(rect, cornerRadii, Path.Direction.CW)
            }
            canvas.clipPath(path)
            super.onDraw(canvas)
        }

        fun setCornerRadius(radius: Float) {
            this.cornerRadius = radius
            invalidate()
        }

        fun setTopLeftRadius(radius: Float) {
            this.topLeftRadius = radius
            invalidate()
        }

        fun setTopRightRadius(radius: Float) {
            this.topRightRadius = radius
            invalidate()
        }

        fun setBottomLeftRadius(radius: Float) {
            this.bottomLeftRadius = radius
            invalidate()
        }

        fun setBottomRightRadius(radius: Float) {
            this.bottomRightRadius = radius
            invalidate()
        }

        fun setCircle(circle: Boolean) {
            this.isCircle = circle
            invalidate()
        }
    }
