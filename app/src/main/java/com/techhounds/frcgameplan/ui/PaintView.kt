package com.techhounds.frcgameplan.ui

import android.view.MotionEvent
import android.view.GestureDetector
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import com.techhounds.frcgameplan.activity.WhiteboardActivity
import com.techhounds.frcgameplan.model.FieldElement
import com.techhounds.frcgameplan.model.SerializeableStroke
import com.techhounds.frcgameplan.model.Stroke
import com.techhounds.frcgameplan.util.AppUtils
import com.techhounds.frcgameplan.util.PaintViewUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class PaintView : View {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var xPoints = ArrayList<Float>()
    var yPoints = ArrayList<Float>()

    private var line = Path()
    private var linePaint = Paint()

    private var paintColor = -0x1000000

    private var paintList = listOf(Color.BLACK, Color.BLUE, Color.RED)
    private var paintIndex = 0

    private var lineSize: Float = 5f

    private var isErasing: Boolean = false

    private val clear = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    private val singleDetector: GestureDetector
    private val doubleDetector: GestureDetector

    private var canvas: Canvas? = null
    private var bitmap: Bitmap? = null

    private val util = PaintViewUtils.getInstance()

    init {
        initLinePaint(linePaint, paintColor)
        isErasing = false
        singleDetector = GestureDetector(context, SimpleTapUp())
        doubleDetector = GestureDetector(context, DoubleTapUp())
    }

    private fun initLinePaint(linePaint : Paint, color : Int) : Paint {
        linePaint.color = color
        linePaint.isAntiAlias = true
        linePaint.isDither = true
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.strokeWidth = lineSize
        linePaint.xfermode = null
        return linePaint
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        for(stroke in util.strokes) {
            canvas.drawPath(stroke.path, initLinePaint(Paint(), stroke.paint))
        }
        linePaint.color = paintColor
        if (isErasing)
            linePaint.xfermode = clear
        canvas.drawPath(line, linePaint)
    }

    /**
     * When the User touches this View, it will create a line
     * @param event : The user's action.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (doubleDetector.onTouchEvent(event)) {
            EventBus.getDefault().post(WhiteboardActivity.ToggleMenuEvent())
            false
        } else if (singleDetector.onTouchEvent(event)) {
            line = Path()
            xPoints = ArrayList()
            yPoints = ArrayList()
            false
        } else {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    line.moveTo(event.x, event.y)
                    xPoints.add(event.x)
                    yPoints.add(event.y)
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    line.lineTo(event.x, event.y)
                    xPoints.add(event.x)
                    yPoints.add(event.y)
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    if (xPoints.size > 2) {
                        canvas!!.drawPath(line, linePaint)
                        util.strokes.add(Stroke(xPoints, yPoints, line, paintColor))
                        AppUtils.instance.removeStack.add(FieldElement.STROKE)
                        EventBus.getDefault().post(WhiteboardActivity.SetColorEvent())
                    }
                    line = Path()
                    xPoints = ArrayList()
                    yPoints = ArrayList()
                    invalidate()
                }
                else -> return false
            }
            invalidate()
            true
        }
    }

    fun setColor(newColor: String) {
        paintColor = Color.parseColor(newColor)
        setColor(paintColor)
    }

    fun setColor(color: Int) {
        paintColor = color
        linePaint.color = color
        invalidate()
    }

    fun setColor(color: Int, erase: Boolean) {
        isErasing = erase
        if (isErasing) {
            linePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            invalidate()
        } else {
            setColor(color)
        }
    }

    fun reset() {
        canvas!!.drawColor(0, PorterDuff.Mode.CLEAR)
        line.reset()
        util.strokes.clear()

        invalidate()
    }

    fun undo() {
        if(util.strokes.size > 0) {
            util.strokes.remove(util.strokes.last())
        } else {
            Toast.makeText(context, "Exceeded Undo Limit", Toast.LENGTH_SHORT).show()
            // TODO: String resource
        }
        invalidate()
    }

    fun write(outputStream : ObjectOutputStream) {
        outputStream.writeObject(util.getSerializeableStrokes())
    }

    fun read(inputStream : ObjectInputStream) {
        var input = inputStream.readObject() as ArrayList<SerializeableStroke>
        util.parseSerializeableStrokes(input)
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        EventBus.getDefault().unregister(this)
        super.onDetachedFromWindow()
    }

    private inner class SimpleTapUp : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean { return true }
        override fun onDoubleTap(event: MotionEvent): Boolean { return false }
    }

    private inner class DoubleTapUp : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean { return false }
        override fun onDoubleTap(event: MotionEvent): Boolean { return true }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTogglePaintEvent(event: TogglePaintEvent) {
        var color = paintList[++paintIndex % paintList.size]
        setColor(color)
        if(paintIndex % paintList.size == 0) color = Color.WHITE
        event.menuItem.setColorFilter(color)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUndoEvent(event: UndoEvent) {
        undo()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onResetEvent(event: ResetEvent) {
        reset()
    }

    class TogglePaintEvent(var menuItem : MenuItemView)
    class UndoEvent
    class ResetEvent
}