package com.rcmdev.decisionmaker.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toPointF
import com.rcmdev.decisionmaker.R
import com.google.android.material.color.MaterialColors
import kotlin.collections.ArrayList
import kotlin.math.*

private const val ONE_DEGREE_IN_RADIANS = 0.01745329251

class RouletteView : View {
    private class RouletteAttributes{
        var paintPalette = ArrayList<Paint>()
        var paintHighlight = Paint(Paint.ANTI_ALIAS_FLAG)
        var paintChooser = Paint(Paint.ANTI_ALIAS_FLAG)
        var paintForeground = Paint(Paint.ANTI_ALIAS_FLAG)


        /**
         * This function calculates the paint brushes for coloring
         * the roulette, giving a degraded effect
         */
        fun setPaintBrush(n:Int, colorSettings:String = "red"){
            paintPalette.clear()

            val colorStart:Int
            val colorEnd:Int

            when(colorSettings){
                "red" -> {
                    colorStart = Color.rgb(255,0,0)
                    colorEnd = Color.rgb(255, 160, 0)
                }
                "orange" -> {
                    colorStart = Color.rgb(255,100,0)
                    colorEnd = Color.rgb(255, 200, 0)
                }
                "purple" -> {
                    colorStart = Color.rgb(85,0,255)
                    colorEnd = Color.rgb(200, 0, 255)
                }
                "blue" -> {
                    colorStart = Color.rgb(0,200,255)
                    colorEnd = Color.rgb(0, 0, 255)
                }
                "yellow" -> {
                    colorStart = Color.rgb(255,200,0)
                    colorEnd = Color.rgb(255, 255, 0)
                }
                "green" -> {
                    colorStart = Color.rgb(120,255,0)
                    colorEnd = Color.rgb(0, 255, 200)
                }
                "multi" -> {
                    colorStart = Color.rgb(255,0,0)
                    colorEnd = Color.rgb(0, 255, 255)
                }
                "greyscale" -> {
                    colorStart = Color.rgb(0,0,0)
                    colorEnd = Color.rgb(202, 200, 200)
                }
                else -> {
                    colorStart = Color.rgb(255,0,0)
                    colorEnd = Color.rgb(255, 160, 0)
                }
            }

            val (r1, g1, b1) = listOf(Color.red(colorStart), Color.green(colorStart), Color.blue(colorStart))
            val (r2, g2, b2) = listOf(Color.red(colorEnd), Color.green(colorEnd), Color.blue(colorEnd))

            if(n <= 1){
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.color = Color.rgb((r1 + r2)/2, (g1 + g2)/2, (b1 + b2)/2)
                paintPalette.add(paint)
                return
            }

            for(i in 0 until n){
                val p = Paint(Paint.ANTI_ALIAS_FLAG)
                val r = r1 + i*(r2-r1)/(n-1)
                val g = g1 + i*(g2-g1)/(n-1)
                val b = b1 + i*(b2-b1)/(n-1)

                p.color = Color.rgb(r, g, b)
                paintPalette.add(p)
            }
        }
    }

    private class RouletteViewGeometry{
        var o:PointF = PointF(0f,0f)
        var size = PointF(0f, 0f)
            set(value){
                field = value

                diameter = min(value.x, value.y)
                outerR = diameter / 2f
                innerR = 0.96f*outerR
                o = PointF(value.x/2f, value.y/2f)

                chooserPath = Path()
                chooserPath.moveTo(o.x, o.y - outerR*0.8f)
                chooserPath.lineTo(o.x + outerR*0.06f, o.y - outerR + ((outerR-innerR)/2f))
                chooserPath.lineTo(o.x - outerR*0.06f, o.y - outerR + ((outerR-innerR)/2f))
                chooserPath.lineTo(o.x, o.y - outerR*0.8f)
                chooserPath.close()

                arcBoxLT = PointF(o.x - innerR, o.y - innerR)
                arcBoxRB = PointF(o.x + innerR, o.y + innerR)
            }

        var innerR = 0.4f
        var outerR = 0.5f
        var diameter = 1f

        var scale = 1f
        var rotation = 0f
        var partitionStep = 360f

        var chooserPath = Path()
        var arcBoxLT = PointF(0f, 0f)
        var arcBoxRB = PointF(0f, 0f)

        private var partitionDivisions = ArrayList<PointF>()

        /**
         *  Calculate roulette partition step angle, and calculate partitions division lines
         *  @param n are the number of partitions
         */
        fun setPartitions(n: Int){
            if(n == 0) return
            partitionStep = 360f / n.toFloat()
            partitionDivisions.clear()
            if(n > 1) {
                for (i in 0 until n) {
                    val t = ONE_DEGREE_IN_RADIANS * i * partitionStep
                    val lx = innerR * cos(t).toFloat() + o.x
                    val ly = innerR * sin(t).toFloat() + o.y
                    partitionDivisions.add(PointF(lx, ly))
                }
            }
        }

        /**
         * Get the Partition division line end point
         * @param idx is the partition index
         */
        fun getPartitionDivision(idx:Int):PointF{
            if(partitionDivisions.size == 0) return PointF(0f, 0f)
            return partitionDivisions[idx]
        }
    }

    private var attrs = RouletteAttributes()
    private var geom = RouletteViewGeometry()

    private var rouletteOptions: ArrayList<String> = ArrayList()
    var onRouletteViewListener:OnRouletteViewListener? = null

    constructor(context: Context?) : super(context) {
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    /**
     * Function for initializing the class (avoids repeated code in constructor)
     */
    private fun init() {
        setBackgroundColor(Color.TRANSPARENT)
        attrs.setPaintBrush(rouletteOptions.size)
        attrs.paintHighlight.color = Color.WHITE
        attrs.paintHighlight.textSize = 70f
        attrs.paintHighlight.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        attrs.paintHighlight.strokeWidth = 5f
        attrs.paintChooser.color = MaterialColors.getColor(this, R.attr.colorSecondary)
        attrs.paintForeground.color = MaterialColors.getColor(this, R.attr.colorSecondary)
    }

    /**
     * Overrides the onSizeChanged fxn
     * @param w is the new view width
     * @param h is the new view height
     * @param oldw is the old view width
     * @param oldh is the old view height
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        geom.size = Point(w, h).toPointF()
        geom.setPartitions(rouletteOptions.size)
    }

    /**
     * Overrides the onMeasure fxn -> Responsive View
     * @param widthMeasureSpec is the view width
     * @param heightMeasureSpec is the view height
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val wSpec = MeasureSpec.getSize(widthMeasureSpec) - (paddingLeft + paddingRight)
        val hSpec = MeasureSpec.getSize(heightMeasureSpec) - (paddingTop + paddingBottom)

        val wDes = min(wSpec, hSpec)
        if(!isInEditMode) {
            setMeasuredDimension(wDes, wDes)
        }else{
            setMeasuredDimension(800, 800)
        }
    }

    /**
     * Overrides View onDraw fxn
     * @param canvas is the surface provided to draw.
     */
    override fun onDraw(canvas: Canvas) {
        canvas.scale(geom.scale, geom.scale, geom.o.x, geom.o.y)
        canvas.drawCircle(geom.o.x, geom.o.y, geom.outerR, attrs.paintForeground)

        if(rouletteOptions.size == 0) {
            val t1Off = getTextBoxOffset("Add")
            val t2Off = getTextBoxOffset("choices")
            canvas.drawCircle(geom.o.x, geom.o.y, geom.innerR, attrs.paintPalette[0])
            canvas.drawText("Add", geom.o.x - t1Off.x, geom.o.y - (t1Off.y*1.5).toInt(), attrs.paintHighlight)
            canvas.drawText("choices", geom.o.x - t2Off.x, geom.o.y + (t2Off.y*1.5).toInt(),  attrs.paintHighlight)
            canvas.drawPath(geom.chooserPath, attrs.paintChooser)
            return
        }

        canvas.save()
        canvas.rotate(geom.rotation - 90, geom.o.x, geom.o.y)

        var startAngle = 0f
        for(i in 0 until rouletteOptions.size){
            canvas.drawArc(geom.arcBoxLT.x, geom.arcBoxLT.y, geom.arcBoxRB.x, geom.arcBoxRB.y, startAngle, geom.partitionStep, true, attrs.paintPalette[i])
            startAngle += geom.partitionStep
        }

        if(rouletteOptions.size > 1) {
            for (i in 0 until rouletteOptions.size) {
                val line = geom.getPartitionDivision(i)
                canvas.drawLine(geom.o.x, geom.o.y, line.x, line.y, attrs.paintHighlight)
            }

            canvas.rotate(geom.partitionStep/2, geom.o.x, geom.o.y)
            val deltaWidth = geom.innerR / 4f
            val maxWidth = geom.o.x + geom.innerR - (geom.o.x + geom.innerR / 3.8f)
            val maxHeight = calculateMaxHeight(deltaWidth, deltaWidth, geom.partitionStep)

            for (choice in rouletteOptions) {
                attrs.paintHighlight.textSize = calculateFontSize(choice, attrs.paintHighlight, maxWidth, maxHeight)
                val deltaHeight = getTextRealHeight(choice)

                canvas.drawText(choice, geom.o.x + deltaWidth, geom.o.y + deltaHeight, attrs.paintHighlight)
                canvas.rotate(geom.partitionStep, geom.o.x, geom.o.y)
            }
        }else{
            val xOff = getTextBoxOffset(rouletteOptions[0]).x
            canvas.drawText(rouletteOptions[0], geom.o.x - xOff, geom.o.y, attrs.paintHighlight)
        }
        canvas.restore()
        canvas.drawPath(geom.chooserPath, attrs.paintChooser)
    }

    /**
     * Calculate the text font size, depending on the size of the string and max values.
     * @param text is the string to calculate its font size
     * @param paint is the Paint object
     * @param maxWidth is the maximum length of X axis
     * @param maxHeight is the maximum length of Y axis
     * @return the fitted font size
     */
    private fun calculateFontSize(text: String, paint: Paint, maxWidth: Float, maxHeight: Float): Float{
        val bound = Rect()
        var size = 20.0f
        val step = 10.0f
        paint.textSize = size
        while(true){
            paint.getTextBounds(text, 0, text.length, bound)
            if(bound.width() < maxWidth && bound.height() < maxHeight){
                size += step
                paint.textSize = size
            }else{
                return size - step
            }
        }
    }

    /**
     * Get the current choice, which the triangle is pointing to
     */
    private fun getRouletteIndex():Int{
        if(rouletteOptions.size == 0) return -1
        if(rouletteOptions.size == 1) return  0
        return floor(rouletteOptions.size - geom.rotation/geom.partitionStep).toInt() % rouletteOptions.size
    }

    private var animationStarted = false
    private var lastStep = 0

    fun isAnimationRunning() : Boolean{ return animationStarted }

    private lateinit var animator: ValueAnimator

    /**
     * Start spin animation
     * @param ms is the animation duration in milliseconds
     */
    fun spin(ms:Long, speed:Float=1f){
        if(!animationStarted){
            animationStarted = true
            val tEnd = ms / 1000f
            ValueAnimator.setFrameDelay(50)
            animator = ValueAnimator.ofFloat(0f, tEnd)

            animator.duration = ms
            animator.addUpdateListener {
                val t = it.animatedValue as Float
                val b = speed*(tEnd - t)
                geom.rotation = (geom.rotation + b)%360
                val st = getRouletteIndex()

                if(lastStep != st && st >= 0){
                    onRouletteViewListener?.onRouletteOptionChanged()
                    lastStep = st
                }
                invalidate()
            }
            animator.addListener(spinAnimationListener)
            animator.start()
        }
    }

    /**
     * Stop spin animation
     */
    fun stopSpinning(){
        if(animationStarted){
            animationStarted = false
            animator.cancel()
        }
    }

    /**
     * Spin animation listener, implements the onAnimationEnd listener
     */
    private val spinAnimationListener = object:Animator.AnimatorListener{
        override fun onAnimationEnd(animation: Animator?) {
            if(animationStarted) {
                animationStarted = false
                if (rouletteOptions.size == 0) return
                if (rouletteOptions.size == 1) {
                    onRouletteViewListener?.onRouletteSpinCompleted(0, rouletteOptions[0])
                    return
                }

                geom.rotation %= 360f
                val idx = getRouletteIndex()

                onRouletteViewListener?.onRouletteSpinCompleted(idx, rouletteOptions[idx])
            }
        }
        override fun onAnimationStart(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationRepeat(animation: Animator?) {}
    }

    /**
     * Set the Roulette Option list, calculate partitions
     * and colors, then redraw
     * @param options is the option list
     */
    fun setRouletteOptionList(options:ArrayList<String>, colorSettings:String){
        rouletteOptions = options
        geom.setPartitions(rouletteOptions.size)
        attrs.setPaintBrush(rouletteOptions.size, colorSettings)
        invalidate()
    }

    /**
     * Get the roulette options count
     */
    fun getRouletteOptionsCount():Int{ return rouletteOptions.size }

    /**
     * Get the roulette rotation
     */
    fun getRouletteRotation():Float{ return geom.rotation }

    /**
     * Set the Roulette rotation
     * @param rotation is the roulette rotation
     */
    fun setRouletteRotation(rotation:Float){ geom.rotation = rotation }

    /**
     * Get text bounds depending on highlightPaint textSize,
     * then calculate the x and y relative centers
     * @param txt is the text string for calculating its bounds
     * @return Pair(x:Int,y:Int)
     */
    private fun getTextBoxOffset(txt:String) : Point{
        val bounds = Rect()
        attrs.paintHighlight.getTextBounds(txt, 0, txt.length, bounds)
        return Point(bounds.width()/2, bounds.height()/2)
    }

    /**
     * Get half of the text height depending on highlightPaint textSize,
     * bounds.height != (bounds.top - bounds.bottom), that's why is the real height.
     * @param txt is the text string for calculating its half-height
     * @return text real height divided by two
     */
    private fun getTextRealHeight(txt:String): Int{
        val bounds = Rect()
        attrs.paintHighlight.getTextBounds(txt, 0, txt.length, bounds)
        return ((bounds.top*(-1)) - bounds.bottom) / 2
    }

    private var moveLastPoint = PointF(0f, 0f)
    private var actionDown = false
    private var actionDownOutRt = false
    private var rotSpeed = 1f
    private var tStart:Long = 0
    private var moved = false

    /**
     * Overrides the touch event function
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val p = PointF(event?.x!! - geom.o.x, event.y - geom.o.y)

        when(event.action.and(MotionEvent.ACTION_MASK)){
            MotionEvent.ACTION_UP -> {
                if((actionDown || actionDownOutRt) && moved) onRouletteViewListener?.onRouletteSpinEvent(rotSpeed)
                actionDown = false
                actionDownOutRt = false
                return false
            }
            MotionEvent.ACTION_DOWN->{
                if(animationStarted) return false
                if((p.length() <= geom.outerR) && (p.length() > 1)) {
                    moveLastPoint = PointF(p.x, p.y)
                    rotSpeed = 1f
                    actionDown = true
                    moved = false
                    tStart = System.currentTimeMillis()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (animationStarted) return false

                if (p.length() > geom.outerR || p.length() < 1f) {
                    actionDown = false
                    actionDownOutRt = true
                    return false
                } else if (!actionDown && (p.length() <= geom.outerR)) {
                    actionDown = true
                    moveLastPoint = PointF(p.x, p.y)
                    rotSpeed = 1f
                    tStart = System.currentTimeMillis()
                    actionDownOutRt = false
                    moved = false
                    return true
                } else if (!actionDown) return false

                val t = System.currentTimeMillis()
                val dt = t - tStart
                tStart = t

                val a = moveLastPoint.phase(p)

                rotSpeed = a/dt.toFloat()
                geom.rotation = (geom.rotation + a) % 360f
                moveLastPoint = p
                actionDownOutRt = false
                moved = true
            }
            else->{ return false }
        }
        invalidate()
        return true
    }

    /**
     * Extend PointF function for calculating the
     * scalar product with a vector p
     * @param p is the second vector
     * @return (x,y)·p
     */
    private fun PointF.scalarProduct(p:PointF) : Float{ return this.x*p.x + this.y*p.y }

    /**
     * Extend PointF function for calculating the
     * z component of the cross product with a vector p
     * @param p is the second vector
     * @return ((x,y) x p).z
     */
    private fun PointF.crossProductZ(p:PointF) : Float{ return this.x*p.y - this.y*p.x }

    /**
     * Extend PointF function for calculating the
     * angle (α) formed with vector p, taking into account
     * the angle direction (clockwise or counterclockwise)
     * @param p is the second vector
     * @return α in degrees
     */
    private fun PointF.phase(p:PointF) : Float{
        val cosa = max(-1f, min(1f,this.scalarProduct(p)/(this.length() * p.length())))
        return 57.2957795131f*acos(cosa)*sign(this.crossProductZ(p))
    }

    /**
     * Law of cosines. a^2 = b^2 + c^2 - 2bc cosA
     * @param A is the angle between a and b
     * @param b is the a side of the triangle
     * @param c is the b side of the triangle
     * @return the a side of the triangle
     */
    private fun calculateMaxHeight(b: Float, c: Float, A: Float): Float {
        val angleRadians = A * ONE_DEGREE_IN_RADIANS
        return sqrt(b.pow(2) + c.pow(2) - (2*b*c * cos(angleRadians))).toFloat()
    }
}



