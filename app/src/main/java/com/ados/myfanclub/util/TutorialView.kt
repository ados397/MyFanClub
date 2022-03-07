package com.ados.myfanclub.util

import android.content.Context
import android.graphics.*
import android.view.View

class TutorialView(context: Context) : View(context) {
    private val path = Path()
    private val rect = RectF()
    private val stroke = Paint().apply {
        isAntiAlias = true
        strokeWidth = 4f
        color = Color.WHITE
        style = Paint.Style.STROKE
    }
    private val eraser = Paint().apply {
        isAntiAlias = true
        alpha = 99
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val holeBorderRadius = 10f
    private val holeWidth = 200
    private val holeHeight = 200

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBorder(canvas)
        drawHole(canvas)
    }

    private fun drawBorder(canvas: Canvas) {
        path.rewind()
        path.addRoundRect(
            rect.apply {
                setRect(4f/*border width*/)
            },
            holeBorderRadius,
            holeBorderRadius,
            Path.Direction.CW
        )

        canvas.drawPath(path, stroke)
    }

    private fun drawHole(canvas: Canvas) {
        canvas.drawRoundRect(
            rect.apply {
                setRect()
            },
            holeBorderRadius, holeBorderRadius, eraser
        )
    }

    private fun setRect(offset: Float = 0f) {
        rect.set(
            ((width - holeWidth)/2) - offset,
            ((height - holeHeight)/2) - offset,
            ((width - holeWidth)/2 + holeWidth) + offset,
            ((height - holeHeight)/2 + holeHeight) + offset
        )
    }
}