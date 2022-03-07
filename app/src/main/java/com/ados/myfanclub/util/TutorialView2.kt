package com.ados.myfanclub.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

class RectOverlayView : LinearLayout {
    private var bitmap: Bitmap? = null
    private var left: Float? = 0f
    private var top: Float? = 0f
    private var right: Float? = 0f
    private var bottom: Float? = 0f

    private val borderWidth = 2.toFloat()

    private var mode: MODE = MODE.DEFAULT

    constructor(
        context: Context?,
        left: Float?,
        top: Float?,
        right: Float?,
        bottom: Float?
    ) : super(context) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom

        z = -1f // 레이아웃 트리 관계에서 최하단에 위치하도록

        visibility = View.INVISIBLE
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun onLayout(
        changed: Boolean,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
        super.onLayout(changed, l, t, r, b)
        bitmap = null
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (bitmap == null) {
            createWindowFrame()
        }
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    protected fun createWindowFrame() {
        bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val osCanvas = Canvas(bitmap!!)
        val outerRectangle =
            RectF(0f, 0f, width.toFloat(), height.toFloat())
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        when (mode) {
            MODE.DEFAULT -> {
                paint.color = Color.BLACK
                paint.alpha = 99
            }
            MODE.DIM -> {
                paint.setMaskFilter(BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL))
                paint.color = Color.BLACK
                paint.alpha = 180
            }
        }

        osCanvas.drawRect(outerRectangle, paint)


        paint.color = Color.TRANSPARENT
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT) // 요 부분이 punch 해주는 효과

        val borderPaint =
            Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.color = Color.WHITE

        if (mode == MODE.DEFAULT) {
            osCanvas.drawRoundRect(
                RectF(
                    left!!.minus(borderWidth),
                    top!!.minus(borderWidth),
                    right!!.plus(borderWidth),
                    bottom!!.plus(borderWidth)
                ), 30f, 30f, borderPaint
            )
        }
        osCanvas.drawRoundRect(RectF(left!!, top!!, right!!, bottom!!), 25f, 25f, paint)
    }


    fun changeMode(mode: MODE) {
        this.mode = mode
        invalidate()
    }


    companion object {
        /**
         * 화면 모드로 1) 기본모드 2) 배경이 안보이는 DIM 모드
         */
        enum class MODE {
            DEFAULT,
            DIM
        }
    }
}