package com.example.decisionmaker

import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.decisionmaker.views.Roulette
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    var myRoulette:Roulette? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myRoulette = findViewById(R.id.roulette2)

        //Runnable for animating the roulette
        val rouletteRotThread = Runnable {
            kotlin.run {
                val animator:ObjectAnimator = ObjectAnimator.ofFloat(myRoulette, "rotation", Random.nextFloat()*720.0f + 720.0f)
                animator.setDuration(2000)
                animator.start()
            }
        }

        //Post delay runnable for 1500 ms
        val handler = Handler(Looper.myLooper()!!)
        handler.postDelayed(rouletteRotThread, 1500)

    }
}