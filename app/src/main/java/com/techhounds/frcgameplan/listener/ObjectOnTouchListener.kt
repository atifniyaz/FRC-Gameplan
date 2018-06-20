package com.techhounds.frcgameplan.listener

import android.view.MotionEvent
import android.view.View
import com.techhounds.frcgameplan.activity.WhiteboardActivity
import com.techhounds.frcgameplan.ui.TeamView
import org.greenrobot.eventbus.EventBus

class ObjectOnTouchListener : View.OnTouchListener {

    override fun onTouch(view: View?, events: MotionEvent?): Boolean {

        if(view!!.parent == null) {
            return false
        }

        var event = events!!
        var width = view!!.width
        var height = view!!.height

        if(event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            var candidateX = event!!.rawX - width / 2
            var candidateY= event!!.rawY - height
            val parent = view.parent as View

            candidateX = if (candidateX <= 0) 0f else candidateX
            candidateX = if (candidateX >= parent.width - width) (parent.width - width).toFloat() else candidateX

            candidateY = if (candidateY <= 0) 0f else candidateY
            candidateY = if (candidateY >= parent.height - height) (parent.height - height).toFloat() else candidateY

            view!!.x = candidateX
            view!!.y = candidateY

            if(candidateX < 10 && candidateY < 10 && view is TeamView) {
                EventBus.getDefault().post(WhiteboardActivity.RemoveFieldObjectEvent(view))
            }
        }
        return true
    }
}