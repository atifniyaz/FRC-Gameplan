package com.techhounds.frcgameplan.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.view_object.view.*

class TeamView : RelativeLayout {

    var imageResourceIndex : Int? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun write() : String {
        var parentWidth = (parent as RelativeLayout).width
        var parentHeight = (parent as RelativeLayout).height

        var ratioX = x
        var ratioY = y
        var tag = tag

        return ratioX.toString() + "," + ratioY.toString() + "," + tag
    }

    fun read(ratioX : Float, ratioY : Float, tag : String) {
        var parentWidth = (parent as RelativeLayout).width
        var parentHeight = (parent as RelativeLayout).height

        x = ratioX
        y = ratioY
        this.tag = tag
    }
}