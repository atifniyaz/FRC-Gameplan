package com.techhounds.frcgameplan.model

import android.graphics.Path
import java.io.Serializable

class SerializeableStroke(var xPoints : ArrayList<Float>, var yPoints : ArrayList<Float>,  var paint : Int) : Serializable {

    fun toStroke() : Stroke {
        val path = Path()

        for (i in 0 until xPoints.size) {
            if (i == 0) {
                path.moveTo(xPoints[i], yPoints[i])
            } else {
                path.lineTo(xPoints[i], yPoints[i])
            }
        }
        return Stroke(xPoints, yPoints, path, paint)
    }
}