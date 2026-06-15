package com.example.grocerly.ui.insights

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.grocerly.R
import kotlin.math.max

class AreaChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paintGrid = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.chart_grid)
    }

    private val paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 6f
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.chart_line)
    }

    private val paintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.chart_fill)
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 34f
        color = ContextCompat.getColor(context, R.color.chart_text)
    }

    private var values: List<Float> = emptyList()

    fun setValues(v: List<Float>) {
        values = v
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val padLeft = 90f
        val padRight = 40f
        val padTop = 30f
        val padBottom = 60f

        val chartW = w - padLeft - padRight
        val chartH = h - padTop - padBottom

        val vCount = 6
        val hCount = 4
        for (i in 0..vCount) {
            val x = padLeft + (chartW / vCount) * i
            canvas.drawLine(x, padTop, x, padTop + chartH, paintGrid)
        }
        for (i in 0..hCount) {
            val y = padTop + (chartH / hCount) * i
            canvas.drawLine(padLeft, y, padLeft + chartW, y, paintGrid)
        }

        if (values.isEmpty()) {
            canvas.drawText(
                context.getString(R.string.chart_zero),
                10f,
                padTop + chartH,
                paintText
            )
            return
        }

        val maxV = max(1f, values.maxOrNull() ?: 1f)

        val maxLabel = formatK(maxV)
        val midLabel = formatK(maxV / 2f)

        canvas.drawText(
            maxLabel,
            10f,
            padTop + 10f + paintText.textSize,
            paintText
        )
        canvas.drawText(
            midLabel,
            10f,
            padTop + chartH / 2f,
            paintText
        )
        canvas.drawText(
            context.getString(R.string.chart_zero),
            10f,
            padTop + chartH,
            paintText
        )

        fun xOf(i: Int): Float =
            padLeft + (chartW / max(1, values.size - 1)) * i

        fun yOf(v: Float): Float =
            padTop + chartH - (v / maxV) * chartH

        val linePath = Path()
        linePath.moveTo(xOf(0), yOf(values[0]))
        for (i in 1 until values.size) {
            linePath.lineTo(xOf(i), yOf(values[i]))
        }

        val fillPath = Path(linePath)
        fillPath.lineTo(xOf(values.size - 1), padTop + chartH)
        fillPath.lineTo(xOf(0), padTop + chartH)
        fillPath.close()

        canvas.drawPath(fillPath, paintFill)
        canvas.drawPath(linePath, paintLine)
    }

    private fun formatK(v: Float): String {
        return if (v >= 10000f) {
            "${(v / 1000f).toInt()}${context.getString(R.string.chart_k_suffix)}"
        } else {
            v.toInt().toString()
        }
    }
}
