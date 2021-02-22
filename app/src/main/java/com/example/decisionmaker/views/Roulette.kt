package com.example.decisionmaker.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class Roulette : View {
    private var rouletteColors: ArrayList<Paint> = ArrayList()
    private var rouletteOptions: ArrayList<String> = ArrayList()
    private var highlightPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var startColor = arrayOf(255, 0, 0)
    private var endColor = arrayOf(255, 200, 0)

    private var tSize = 0

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
        highlightPaint.color = Color.WHITE
        highlightPaint.textSize = 70f
        highlightPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        highlightPaint.strokeWidth = 5f
        tSize = getTextBoxOffset("o").second
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
        val n = rouletteOptions.size

        //Get canvas center
        val cx = width/2.0f
        val cy = height/2.0f

        //Get roulette shape max dimension and radius
        val dim = min(width, height).toFloat()
        val radius = dim/2

        //Empty roulette, draw single circle with advice text
        if(n == 0) {
            val t1Off = getTextBoxOffset("Ruleta")
            val t2Off = getTextBoxOffset("vacia")
            canvas.drawCircle(cx, cy, radius, rouletteColors[0])
            canvas.drawText("Ruleta", cx - t1Off.first, cy - (t1Off.second*1.5).toInt(), highlightPaint)
            canvas.drawText("vacia", cx - t2Off.first, cy + (t2Off.second*1.5).toInt(), highlightPaint)
            return
        }

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
            val xOff = getTextBoxOffset(rouletteOptions[0]).first
            canvas.drawText(rouletteOptions[0], cx - xOff, cy, highlightPaint)
        }
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
    private fun getTextBoxOffset(txt:String) : Pair<Int, Int>{
        val bounds = Rect()
        highlightPaint.getTextBounds(txt, 0, txt.length, bounds)
        return Pair(bounds.width()/2, bounds.height()/2)
    }

}