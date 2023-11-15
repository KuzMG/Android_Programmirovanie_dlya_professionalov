package com.example.draganddraw

import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable

class Box(val start: PointF)  {
    val rotate: Float
        get() {
            if(xEnd!=null && xStart!=null) {
                val length = xEnd!! - xStart!!
                return length/2F
            }
            else {
                return 0F
            }
        }
    var xStart: Float? = null
    var xEnd: Float? = null
    var end: PointF = start
    val left: Float
        get() = Math.min(start.x,end.x)
    val right: Float
        get() = Math.max(start.x,end.x)
    val top: Float
        get() = Math.min(start.y,end.y)
    val bottom: Float
        get() = Math.max(start.y,end.y)

}