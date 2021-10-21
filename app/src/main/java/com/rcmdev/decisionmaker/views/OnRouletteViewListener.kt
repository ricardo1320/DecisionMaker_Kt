package com.rcmdev.decisionmaker.views

interface OnRouletteViewListener {
    fun onRouletteSpinCompleted(idx:Int, choice:String)
    fun onRouletteSpinEvent(speed:Float)
    fun onRouletteOptionChanged()
}