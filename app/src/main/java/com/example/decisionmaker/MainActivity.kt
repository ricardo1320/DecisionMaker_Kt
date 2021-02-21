package com.example.decisionmaker

import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import kotlin.random.Random
import kotlinx.android.synthetic.main.activity_main.*

//Tag for LOG
private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    //Initialize variables for the values of animation (spinning the roulette)
    var startPoint: Float = 0.0f
    var endPoint: Float = Random.nextFloat()*720.0f + 720.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Post delay runnable for 1500 ms
        //val handler = Handler(Looper.myLooper()!!)
        //handler.postDelayed(rouletteRotThread, 1500)

        //Click listener for button_spin (spin the roulette)
        val listener = View.OnClickListener { view ->
            when(view.id){
                R.id.button_spin -> {
                    spinRoulette(startPoint, endPoint)
                    startPoint = endPoint
                    endPoint += Random.nextFloat()*720.0f + 720.0f
                }
            }
        }

        button_spin.setOnClickListener(listener)

    }

    //Function for animating the roulette
    private fun spinRoulette(start: Float, end: Float){
        val animator: ObjectAnimator = ObjectAnimator.ofFloat(roulette, "rotation",start, end)
        animator.setDuration(2000)
        //Log the values: start and end point of the animation
        Log.d("MainActivity", "values: ${animator.values[0]}")
        animator.start()
    }

}