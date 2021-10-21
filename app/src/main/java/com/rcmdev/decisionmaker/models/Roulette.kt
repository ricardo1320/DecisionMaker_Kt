package com.rcmdev.decisionmaker.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Roulette (val name: String, val options: ArrayList<String>): Parcelable
