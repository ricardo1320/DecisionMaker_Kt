package com.example.decisionmaker.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//Data class to hold Roulette object.
@Parcelize
data class Roulette (val name: String, val options: ArrayList<String>): Parcelable
