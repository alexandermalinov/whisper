package com.example.whisper.ui.customviews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView


class BlurSurfaceView : View {
    private var canvas: Canvas? = null
    private lateinit var bitmap: Bitmap
    private lateinit var parent: ViewGroup
    private var renderScript: RenderScript? = null
    private lateinit var blurScript: ScriptIntrinsicBlur
    private lateinit var outAllocation: Allocation

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        init(measuredWidth, measuredHeight)
    }

    private fun init(measuredWidth: Int, measuredHeight: Int) {
        if (measuredWidth <= 0 || measuredHeight <= 0) return
        bitmap = Bitmap.createBitmap(
            measuredWidth,
            measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas = Canvas(bitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (::bitmap.isInitialized.not()) return
        canvas.save()
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.restore()
    }

    private fun getBackgroundAndDrawBehind() {
        //Arrays to store the co-ordinates
        val rootLocation = IntArray(2)
        val viewLocation = IntArray(2)
        parent.getLocationOnScreen(rootLocation) //get the parent co-ordinates
        this.getLocationOnScreen(viewLocation) //get view co-ordinates
        //Calculate relative co-ordinates
        val left: Int = viewLocation[0] - rootLocation[0]
        val top: Int = viewLocation[1] - rootLocation[1]
        canvas?.save()
        canvas?.translate(-left.toFloat(), -top.toFloat()) //translates the initial position
        canvas?.let {
            parent.draw(it)
        }
        canvas?.restore()
        blurWithRenderScript()
    }

    private fun blurWithRenderScript() {
        if(::bitmap.isInitialized.not()) return
        renderScript = RenderScript.create(context)
        blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        val inAllocation = Allocation.createFromBitmap(renderScript, bitmap)
        outAllocation = Allocation.createTyped(renderScript, inAllocation.type)
        blurScript.setRadius(25f)
        blurScript.setInput(inAllocation)
        blurScript.forEach(outAllocation)
        outAllocation.copyTo(bitmap)
        inAllocation.destroy()
    }

    fun setParent(parent: ViewGroup) {
        this.parent = parent
        this.parent.viewTreeObserver.removeOnPreDrawListener(drawListener)
        this.parent.viewTreeObserver.addOnPreDrawListener(drawListener)
    }

    private val drawListener =
        ViewTreeObserver.OnPreDrawListener {
            getBackgroundAndDrawBehind()
            true
        }
}

@Composable
fun BlurSurface(
    modifier: Modifier,
    parent: ViewGroup
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent
    ) {
        AndroidView(
            factory = {
                BlurSurfaceView(parent.context)
            },
            modifier = Modifier
                .fillMaxSize(),
            update = { blurView ->
                blurView.setParent(
                    parent = parent
                )
            }
        )
    }
}