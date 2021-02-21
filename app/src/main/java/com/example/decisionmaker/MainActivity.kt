package com.example.decisionmaker

import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import kotlin.random.Random
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Runnable for animating the roulette
        val rouletteRotThread = Runnable {
            kotlin.run {
                //val animator:ObjectAnimator = ObjectAnimator.ofFloat(roulette, "rotation", Random.nextFloat()*720.0f + 720.0f)
                val animator: ObjectAnimator = ObjectAnimator.ofFloat(roulette, "rotation", Random.nextFloat() * 720.0f + 720.0f)
                animator.setDuration(2000)
                animator.start()
            }
        }


        //Post delay runnable for 1500 ms
        //val handler = Handler(Looper.myLooper()!!)
        //handler.postDelayed(rouletteRotThread, 1500)

        //Click listener for button_spin (spin the roulette)
        val listener = View.OnClickListener { view ->
            when(view.id){
                R.id.button_spin -> {
                    rouletteRotThread.run()
                }
            }
        }

        button_spin.setOnClickListener(listener)

    }

}