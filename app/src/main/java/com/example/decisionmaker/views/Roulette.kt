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
import kotlin.collections.ArrayList
import kotlin.math.*
import kotlin.random.Random

class Roulette : View {
    private var rouletteColors: ArrayList<Paint> = ArrayList()
    private var rouletteOptions: ArrayList<String> = ArrayList()
    private var highlightPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var chooserPaint:Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var rouletteOrientation = 0f
    private var rouletteScale = 1f

    private var startColor = arrayOf(255, 0, 0)
    private var endColor = arrayOf(255, 200, 0)

    private var tSize = 0

    var onRouletteViewListener:OnRouletteViewListener? = null

    constructor(context: Context?) : super(context) {
        init(null)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    /**
     * Function for initializing the class (avoids repeated code in constructor)
     * @param attrs is the attribute set that describes the view
     */
    private fun init(attrs: AttributeSet?) {
        setPaintBrush()
        setBackgroundColor(Color.TRANSPARENT)
        highlightPaint.color = Color.WHITE
        highlightPaint.textSize = 70f
        highlightPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        highlightPaint.strokeWidth = 5f

        chooserPaint.color = Color.DKGRAY

        tSize = getTextBoxOffset("o").y
    }

    /**
     * This function calculates the paint brushes for coloring
     * the roulette, giving a degraded effect
     */
    private fun setPaintBrush(){
        rouletteColors.clear()

        if(rouletteOptions.size <= 1){
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.rgb((startColor[0] + endColor[0])/2, (startColor[1] + endColor[1])/2, (startColor[2] + endColor[2])/2)
            rouletteColors.add(paint)

            return
        }

        val clr = arrayOf(0,0,0)
        for(i in 0 until rouletteOptions.size){
            //Get color by linear interpolation between start and end colors
            for(x in 0 until 3){
                clr[x] = startColor[x] + ((endColor[x]-startColor[x])*i)/(rouletteOptions.size-1)
            }
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.color = Color.rgb(clr[0], clr[1], clr[2])
            rouletteColors.add(p)
        }
    }


    /**
     * Overrides View onDraw fxn
     * @param canvas is the surface provided to draw.
     */
    override fun onDraw(canvas: Canvas) {
        //Get canvas center
        val cx = width/2.0f
        val cy = height/2.0f

        val n = rouletteOptions.size

        //Get roulette shape max dimension and radius
        val dim = min(width, height).toFloat()
        val radius = dim/2

        val triangle = Path()
        triangle.moveTo(cx + 0f, cy - radius + 30)
        triangle.lineTo(cx + 20f, cy - radius - 50)
        triangle.lineTo(cx - 20f, cy - radius - 50)
        triangle.lineTo(cx + 0f, cy - radius + 30)
        triangle.close()

        canvas.scale(rouletteScale, rouletteScale, cx, cy)
        //Empty roulette, draw single circle with advice text
        if(n == 0) {
            val t1Off = getTextBoxOffset("Add")
            val t2Off = getTextBoxOffset("choices")
            canvas.drawCircle(cx, cy, radius, rouletteColors[0])
            canvas.drawText("Add", cx - t1Off.x, cy - (t1Off.y*1.5).toInt(), highlightPaint)
            canvas.drawText("choices", cx - t2Off.x, cy + (t2Off.y*1.5).toInt(), highlightPaint)
            canvas.drawPath(triangle, chooserPaint)
            return
        }

        canvas.save()
        canvas.rotate(rouletteOrientation-90, cx, cy)

        //Arc box: left, top corner
        val ax = (width - dim)/2
        val ay = (height - dim)/2

        //Arc box: right, bottom corner
        val dimx = dim + ax
        val dimy = dim + ay

        //Arc angle
        val alfa = 360.0f / n
        var t = -alfa/2.0f

        //Draw choices as 'pieces of cake'
        for(i in 0 until n){
            canvas.drawArc(ax, ay, dimx, dimy, t, alfa, true, rouletteColors[i])
            t +=alfa
        }

        t = -alfa/2.0f
        //Draw lines for choices separation
        if(n > 1) {
            for (i in 0 until n) {
                val lx = radius * cos(Math.toRadians(t.toDouble())).toFloat()
                val ly = radius * sin(Math.toRadians(t.toDouble())).toFloat()
                canvas.drawLine(cx, cy, cx + lx, cy + ly, highlightPaint)
                t += alfa
            }
        }

        //Draw choices text
        if(n > 1) { //Multiple options
            for (choice in rouletteOptions) {
                canvas.drawText(choice, cx + dim / 8, cy + tSize, highlightPaint)
                canvas.rotate(alfa, cx, cy)
            }
        }else{ //Single choice, draw centered text
            val xOff = getTextBoxOffset(rouletteOptions[0]).x
            canvas.drawText(rouletteOptions[0], cx - xOff, cy, highlightPaint)
        }
        canvas.restore()
        canvas.drawPath(triangle, chooserPaint)
    }

    private var animationStarted = false

    /**
     * Start spin animation, random values handled inside this methos
     * @param ms is the animation duration in milliseconds
     */
    fun spin(ms:Long, speed:Float=1f){
        if(!animationStarted){
            animationStarted = true
            val tEnd = ms / 1000f
            val k = 9/tEnd

            val b0 = rouletteOrientation
            val randomRot = 360f*(floor(5.5f/k) + 2.01f*Random.nextFloat())*speed
            val animator = ValueAnimator.ofFloat(0f, tEnd)
            animator.duration = ms

            animator.addUpdateListener {
                val t = it.animatedValue as Float
                val b = randomRot * (1 - exp(-0.8f*t*k))
                rouletteOrientation = (b0 + b)%360
                invalidate()
            }
            animator.addListener(spinAnimationListener)
            animator.start()
        }
    }

    /**
     * Spin animation listener, implements the onAnimationEnd listener
     * calculates the choice index, depending on roulette orientation
     */
    private val spinAnimationListener = object:Animator.AnimatorListener{
        override fun onAnimationEnd(animation: Animator?) {
            animationStarted = false //Free flag for starting another animation process
            if(rouletteOptions.size == 0) return //Empty roulette
            if(rouletteOptions.size == 1){ //Only one option, choose it
                onRouletteViewListener?.OnRouletteSpinCompleted(0, rouletteOptions[0])
                return
            }


            val n = rouletteOptions.size
            rouletteOrientation %= 360f //Valid ranges between 0 and 360 degrees
            val step = 360.0f/n //Angle step size

            //Get index
            val idx = (floor(n - (rouletteOrientation - 0.5*step) / step) %n).toInt()
            Log.d("SPINCOMPLETED", idx.toString() + " " + rouletteOptions[idx])

            //Call spin completed listener
            onRouletteViewListener?.OnRouletteSpinCompleted(idx,rouletteOptions[idx])
        }

        override fun onAnimationStart(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationRepeat(animation: Animator?) {}
    }

    /**
     * Set the Roulette Option list and redraw
     * @param options is the option list
     */
    fun setRouletteOptionList(options:ArrayList<String>){
        rouletteOptions = options
        setPaintBrush()
        invalidate()
    }

    /**
     * Get text bounds depending on highlightPaint textSize,
     * then calculate the x and y relative centers
     * @param txt is the text string for calculating its bounds
     * @return Pair(x:Int,y:Int)
     */
    private fun getTextBoxOffset(txt:String) : Point{
        val bounds = Rect()
        highlightPaint.getTextBounds(txt, 0, txt.length, bounds)
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
        val r = min(width, height)/2f //Roulette radius
        val p = PointF(event?.x!!-width/2f, event.y -height/2f) //Get touch point, with origin at view center

        when(event.action.and(MotionEvent.ACTION_MASK)){
            MotionEvent.ACTION_UP -> {
                if(actionDown || actionDownOutRt){
                    onRouletteViewListener?.OnRouletteSpinEvent(rotSpeed)
                }
                actionDown = false
                actionDownOutRt = false
                return false
            }
            MotionEvent.ACTION_DOWN->{
                Log.d("RT_TOUCH_EVT", "DOWN: " + p.x + ", " + p.y)
                if(animationStarted) return false
                if(p.length() <= r) { //New action down
                    moveLastPoint = PointF(p.x, p.y)
                    rotSpeed = 1f
                    actionDown = true
                    tStart = System.currentTimeMillis()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (animationStarted) return false

                if (p.length() > r) { //Touch event outside the roulette
                    actionDown = false
                    actionDownOutRt = true
                    return false
                } else if (!actionDown && p.length() <= r) { //New action down event
                    actionDown = true
                    moveLastPoint = PointF(p.x, p.y)
                    rotSpeed = 1f
                    tStart = System.currentTimeMillis()
                    actionDownOutRt = false
                    return true
                } else if (!actionDown) { //Not action down, then return
                    return false
                }
                val t = System.currentTimeMillis()
                val dt = t - tStart
                tStart = t

                val pp =  moveLastPoint.x*p.x + moveLastPoint.y*p.y //Scalar product v1·v2
                val cosa = pp / (moveLastPoint.length()*p.length()) //cos(α) = v1·v2/(|v1|·|v2|)
                var a = 180f * (acos(cosa)) / PI.toFloat() //Angle between vectors in degrees

                //Rotation direction given by the sign of the z component of the
                //cross product between the vectors
                //  - (v1 x v2).z < 0 -> then counterclockwise
                //  - (v1 x v2).z > 0 -> then clockwise
                a *= sign(moveLastPoint.x * p.y - moveLastPoint.y * p.x)
                rotSpeed = a/dt.toFloat()
                rouletteOrientation = (rouletteOrientation + a) % 360f
                moveLastPoint = p
                actionDownOutRt = false
            }
            else->{ return false }
        }
        invalidate()
        return true
    }

}


