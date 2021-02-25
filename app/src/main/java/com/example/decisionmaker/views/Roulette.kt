package com.example.decisionmaker.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toPointF
import kotlin.collections.ArrayList
import kotlin.math.*

class Roulette : View {
    private class RouletteAttributes{
        var paintPalette = ArrayList<Paint>()
        var paintHighlight = Paint(Paint.ANTI_ALIAS_FLAG)
        var paintChooser = Paint(Paint.ANTI_ALIAS_FLAG)


        /**
         * This function calculates the paint brushes for coloring
         * the roulette, giving a degraded effect
         */
        fun setPaintBrush(n:Int, colorStart:Int = Color.rgb(255,0,0), colorEnd:Int = Color.rgb(255, 160, 0)){
            paintPalette.clear()

            val (r1, g1, b1) = listOf(Color.red(colorStart), Color.green(colorStart), Color.blue(colorStart))
            val (r2, g2, b2) = listOf(Color.red(colorEnd), Color.green(colorEnd), Color.blue(colorEnd))

            if(n <= 1){
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.color = Color.rgb((r1 + r2)/2, (g1 + g2)/2, (b1 + b2)/2)
                paintPalette.add(paint)
                return
            }

            for(i in 0 until n){
                //Get color by linear interpolation between start and end colors
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
        var o:PointF = PointF(0f,0f)    //View origin
        var size = PointF(0f, 0f)       //View size (width, height)
            set(value){
                field = value

                diameter = min(value.x, value.y) //Roulette diameter
                radius = diameter / 2f
                o = PointF(value.x/2f, value.y/2f) //Origin at the view center

                chooserPath = Path() //Triangle
                chooserPath.moveTo(o.x + 00f, o.y - radius + 30)
                chooserPath.lineTo(o.x + 20f, o.y - radius - 50)
                chooserPath.lineTo(o.x - 20f, o.y - radius - 50)
                chooserPath.lineTo(o.x + 00f, o.y - radius + 30)
                chooserPath.close()

                //Partitions Boxes LT:Left-Top; RB:Right-Bottom
                arcBoxLT = PointF(o.x - radius, o.y - radius)
                arcBoxRB = PointF(o.x + radius, o.y + radius)
            }

        var radius = 0.5f           //Roulette Radius
        var diameter = 1f           //Roulette diameter

        var scale = 1f              //Scale factor
        var rotation = 0f           //Roulette rotation angle
        var partitionStep = 360f    //Partition step angle

        var chooserPath = Path()    //Chooser path
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
                    val t = 0.01745329251 * i * partitionStep // Angle in radians
                    val lx = radius * cos(t).toFloat() + o.x  // Line end Point.x
                    val ly = radius * sin(t).toFloat() + o.y  // Line end Point.y
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

    //Roulette attributes
    private var attrs = RouletteAttributes()
    private var geom = RouletteViewGeometry()

    private var tSize = 0
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
     * @param attributes is the attribute set that describes the view
     */
    private fun init() {
        setBackgroundColor(Color.TRANSPARENT)   //Avoid phone theme apply to the view

        attrs.setPaintBrush(rouletteOptions.size)
        attrs.paintHighlight.color = Color.WHITE
        attrs.paintHighlight.textSize = 70f
        attrs.paintHighlight.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        attrs.paintHighlight.strokeWidth = 5f

        attrs.paintChooser.color = Color.DKGRAY

        tSize = getTextBoxOffset("o").y
    }

    /**
     * Overrides the onSizeChanged fxn
     * @param w is the new view width
     * @param h is the new view height
     * @param oldw is the old view width
     * @param oldh is the old view height
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        geom.size = Point(w, h).toPointF()       //Update roulette geometry attributes
        geom.setPartitions(rouletteOptions.size) //Since partitions depends of size attributes, update them
    }

    /**
     * Overrides View onDraw fxn
     * @param canvas is the surface provided to draw.
     */
    override fun onDraw(canvas: Canvas) {
        canvas.scale(geom.scale, geom.scale, geom.o.x, geom.o.y)

        if(rouletteOptions.size == 0) { //Empty roulette, draw single circle with advice text
            val t1Off = getTextBoxOffset("Add")
            val t2Off = getTextBoxOffset("choices")
            canvas.drawCircle(geom.o.x, geom.o.y, geom.radius, attrs.paintPalette[0])
            canvas.drawText("Add", geom.o.x - t1Off.x, geom.o.y - (t1Off.y*1.5).toInt(), attrs.paintHighlight)
            canvas.drawText("choices", geom.o.x - t2Off.x, geom.o.y + (t2Off.y*1.5).toInt(),  attrs.paintHighlight)
            canvas.drawPath(geom.chooserPath, attrs.paintChooser)
            return
        }

        canvas.save()
        canvas.rotate(geom.rotation - 90, geom.o.x, geom.o.y)

        var t = 0f
        //Draw choices as 'pieces of cake'
        for(i in 0 until rouletteOptions.size){
            canvas.drawArc(geom.arcBoxLT.x, geom.arcBoxLT.y, geom.arcBoxRB.x, geom.arcBoxRB.y, t, geom.partitionStep, true, attrs.paintPalette[i])
            t += geom.partitionStep
        }

        if(rouletteOptions.size > 1) { //Multiple options
            //Draw lines for choices separation
            for (i in 0 until rouletteOptions.size) {
                val line = geom.getPartitionDivision(i)
                canvas.drawLine(geom.o.x, geom.o.y, line.x, line.y, attrs.paintHighlight)
            }
            //Draw choices text
            canvas.rotate(geom.partitionStep/2, geom.o.x, geom.o.y)
            for (choice in rouletteOptions) {
                canvas.drawText(choice, geom.o.x + geom.diameter / 8, geom.o.y + tSize, attrs.paintHighlight)
                canvas.rotate(geom.partitionStep, geom.o.x, geom.o.y)
            }
        }else{ //Single choice, draw centered text
            val xOff = getTextBoxOffset(rouletteOptions[0]).x
            canvas.drawText(rouletteOptions[0], geom.o.x - xOff, geom.o.y, attrs.paintHighlight)
        }
        canvas.restore()
        canvas.drawPath(geom.chooserPath, attrs.paintChooser)
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
    /**
     * Start spin animation
     * @param ms is the animation duration in milliseconds
     */
    fun spin(ms:Long, speed:Float=1f){
        if(!animationStarted){
            animationStarted = true
            val tEnd = ms / 1000f
            ValueAnimator.setFrameDelay(50)
            val animator = ValueAnimator.ofFloat(0f, tEnd)
            //val k = 1/(speed*speed)

            animator.duration = ms
            animator.addUpdateListener {
                val t = it.animatedValue as Float
                val b = speed*(tEnd - t)
                //val b = speed*(exp((k*x)*(k*x)))
                geom.rotation = (geom.rotation + b)%360
                val st = getRouletteIndex()

                if(lastStep != st && st >= 0){
                    onRouletteViewListener?.OnRouletteOptionChanged()
                    lastStep = st
                }
                invalidate()
            }
            animator.addListener(spinAnimationListener)
            animator.start()
        }
    }

    /**
     * Spin animation listener, implements the onAnimationEnd listener
     */
    private val spinAnimationListener = object:Animator.AnimatorListener{
        override fun onAnimationEnd(animation: Animator?) {
            animationStarted = false                //Free flag for starting another animation process
            if (rouletteOptions.size == 0) return    //Empty roulette
            if (rouletteOptions.size == 1) {          //Only one option, choose it
                onRouletteViewListener?.OnRouletteSpinCompleted(0, rouletteOptions[0])
                return
            }

            geom.rotation %= 360f           //Assert 0° ≤ rotation ≤ 360°
            val idx = getRouletteIndex()    //Get roulette index

            //Call spin completed listener with the idx and choice values as parameters
            Log.d("SPINCOMPLETED", idx.toString() + " " + rouletteOptions[idx])
            onRouletteViewListener?.OnRouletteSpinCompleted(idx,rouletteOptions[idx])
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
    fun setRouletteOptionList(options:ArrayList<String>){
        rouletteOptions = options
        geom.setPartitions(rouletteOptions.size)
        attrs.setPaintBrush(rouletteOptions.size)
        invalidate()
    }

    /**
     * Get the roulette option list
     */
    fun getRouletteOptionList():ArrayList<String>{
        return rouletteOptions
    }

    /**
     * Get the roulette options count
     */
    fun getRouletteOptionsCount():Int{
        return rouletteOptions.size
    }

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


    //Auxiliar variables for touch events
    private var moveLastPoint = PointF(0f, 0f)
    private var actionDown = false
    private var actionDownOutRt = false
    private var rotSpeed = 1f
    private var tStart:Long = 0

    /**
     * Overrides the touch event function
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val p = PointF(event?.x!! - geom.o.x, event.y - geom.o.y) //Get touch point, with origin at view center

        when(event.action.and(MotionEvent.ACTION_MASK)){
            MotionEvent.ACTION_UP -> {
                if(actionDown || actionDownOutRt) onRouletteViewListener?.OnRouletteSpinEvent(rotSpeed)
                actionDown = false
                actionDownOutRt = false
                return false
            }
            MotionEvent.ACTION_DOWN->{
                Log.d("RT_TOUCH_EVT", "DOWN: " + p.x + ", " + p.y)
                if(animationStarted) return false
                if(p.length() in 1f..geom.radius) { //New action down
                    moveLastPoint = PointF(p.x, p.y)
                    rotSpeed = 1f
                    actionDown = true
                    tStart = System.currentTimeMillis()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (animationStarted) return false

                if (p.length() > geom.radius || p.length() < 1f) { //Touch event outside the roulette
                    actionDown = false
                    actionDownOutRt = true
                    return false
                } else if (!actionDown && p.length() <= geom.radius) { //New action down event
                    actionDown = true
                    moveLastPoint = PointF(p.x, p.y)
                    rotSpeed = 1f
                    tStart = System.currentTimeMillis()
                    actionDownOutRt = false
                    return true
                } else if (!actionDown) return false //Not action down, then return

                val t = System.currentTimeMillis()
                val dt = t - tStart
                tStart = t

                //Get angle between vectors
                val a = moveLastPoint.phase(p)

                rotSpeed = a/dt.toFloat()
                geom.rotation = (geom.rotation + a) % 360f
                moveLastPoint = p
                actionDownOutRt = false
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
    private fun PointF.scalarProduct(p:PointF) : Float{
        return this.x*p.x + this.y*p.y
    }

    /**
     * Extend PointF function for calculating the
     * z component of the cross product with a vector p
     * @param p is the second vector
     * @return ((x,y) x p).z
     */
    private fun PointF.crossProductZ(p:PointF) : Float{
        return this.x*p.y - this.y*p.x
    }

    /**
     * Extend PointF function for calculating the
     * angle (α) formed with vector p, taking into account
     * the angle direction (clockwise or counterclockwise)
     * @param p is the second vector
     * @return α in degrees
     */
    private fun PointF.phase(p:PointF) : Float{
        // cos(α) = v1·v2/(|v1|·|v2|)
        // Due to Float precision error, assert 1 ≤ cos(a) ≤ 1
        // Cross product give angle direction: >0 -> clockwise, otherwise -> counterclockwise
        val cosa = max(-1f, min(1f,this.scalarProduct(p)/(this.length() * p.length())))
        return 57.2957795131f*acos(cosa)*sign(this.crossProductZ(p))
    }
}



