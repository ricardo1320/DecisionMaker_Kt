package com.example.decisionmaker.views

interface OnRouletteViewListener {
    fun OnRouletteSpinCompleted(idx:Int, choice:String)
    fun OnRouletteSpinEvent(speed:Float)
    fun OnRouletteOptionChanged()
}