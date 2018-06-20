package com.techhounds.frcgameplan.util

import com.techhounds.frcgameplan.model.SerializeableStroke
import com.techhounds.frcgameplan.model.Stroke

class PaintViewUtils {

    companion object {
        private var inst : PaintViewUtils? = null
        fun getInstance() : PaintViewUtils {
            if (inst == null) {
                inst = PaintViewUtils()
            }
            return inst!!
        }
    }

    var strokes = ArrayList<Stroke>()

    fun getSerializeableStrokes() : ArrayList<SerializeableStroke> {
        var strokes = ArrayList<SerializeableStroke>()
        for(stroke in this.strokes) {
            strokes.add(stroke.toSerializeableStroke())
        }
        return strokes
    }

    fun parseSerializeableStrokes(strokes : ArrayList<SerializeableStroke>) {
        this.strokes = ArrayList()
        for(stroke in strokes) {
            this.strokes.add(stroke.toStroke())
        }
    }
}