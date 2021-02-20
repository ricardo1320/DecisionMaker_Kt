package com.example.decisionmaker.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.collections.ArrayList

class Roulette : View {
    private var rouletteColors: ArrayList<Paint> = ArrayList()
    private var rouletteOptions: ArrayList<String> = ArrayList()
    private var highlightPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var startColor = arrayOf(255, 0, 0)
    private var endColor = arrayOf(255, 200, 0)

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
        pushDummyOptions()

        setPaintBrush()
        highlightPaint.setColor(Color.WHITE)
        highlightPaint.textSize = 70f
        highlightPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD))
        highlightPaint.strokeWidth = 10f
    }

    /**
     * This function calculates the paint brushes for coloring
     * the roulette, giving a degraded effect
     */
    private fun setPaintBrush(){
        rouletteColors.clear()

        val clr = arrayOf(0,0,0)
        for(i in 0..rouletteOptions.size -1){
            //Get color by linear interpolation between start and end colors
            for(x in 0..2){
                clr[x] = startColor[x] + ((endColor[x]-startColor[x])*i)/(rouletteOptions.size-1)
            }
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.setColor(Color.rgb(clr[0], clr[1], clr[2]))
            rouletteColors.add(p)
        }

    }

    /**
     * Overrides View onDraw fxn
     * @param canvas is the surface provided to draw.
     */
    override fun onDraw(canvas: Canvas) {
        if(rouletteOptions.size == 0) return
        val n = rouletteOptions.size

        //Get canvas center
        val cx = width/2.0f
        val cy = height/2.0f

        //Get roulette shape max dimension and radius
        val dim:Float = Math.min(width, height).toFloat()
        val radius = dim/2

        //Arc centers
        val ax = Math.abs(width - dim)/2
        val ay = Math.abs(height - dim)/2

        //Arc boxes
        val dimx = dim + ax
        val dimy = dim + ay

        //Arc angle
        val alfa = 360.0f / n
        var t = -alfa/2.0f

        //Draw choices as 'pieces of cake'
        for(i in 0..n-1){
            canvas.drawArc(ax, ay, dimx, dimy, t, alfa, true, rouletteColors[i])
            t +=alfa
        }

        t = -alfa/2.0f
        //Draw lines for choices separation
        for(i in 0..n-1){
            val lx = radius*Math.cos(Math.toRadians(t.toDouble())).toFloat()
            val ly = radius*Math.sin(Math.toRadians(t.toDouble())).toFloat()
            canvas.drawLine(cx, cy, cx + lx, cy + ly, highlightPaint)
            t += alfa
        }

        //Draw choices text
        for(choice in rouletteOptions){
            canvas.drawText(choice, cx + dim/8, cy + 20, highlightPaint)
            canvas.rotate(alfa, cx, cy)
        }
    }


    /**
     * Insert dummy data to roulette for testing purposes
     */
    private fun pushDummyOptions(){
        rouletteOptions.add("Tacos")
        rouletteOptions.add("Pizza")
        rouletteOptions.add("Sushi")
        rouletteOptions.add("Tortas")
        rouletteOptions.add("Hot dogs")
        rouletteOptions.add("KFC")
    }

}