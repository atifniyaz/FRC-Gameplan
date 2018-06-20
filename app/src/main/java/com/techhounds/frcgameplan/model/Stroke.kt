package com.techhounds.frcgameplan.model

import android.graphics.Path
import android.graphics.Paint

class Stroke(var xPoints : ArrayList<Float>, var yPoints : ArrayList<Float>, var path : Path, var paint : Int) {

    fun toSerializeableStroke() : SerializeableStroke {
        return SerializeableStroke(xPoints, yPoints, paint)
    }
}