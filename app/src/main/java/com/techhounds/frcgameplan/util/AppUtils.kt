package com.techhounds.frcgameplan.util

import android.content.Context
import android.content.res.Resources
import android.app.AlertDialog
import com.techhounds.frcgameplan.model.FieldElement


class AppUtils {
    companion object {
        val instance = AppUtils()
    }

    var removeStack = ArrayList<Int>()

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().displayMetrics.density).toInt()
    }

    fun clearStack() {
        removeStack.clear()
    }

    fun removeTypeFromStack(type : Int, index : Int) {
        var cnt = 0
        var location = -1
        for(i in 0 until removeStack.size) {
            if (removeStack[i] == type) {
                cnt++
            }
            if (cnt == index + 1) {
                location = i
                break
            }
        }
        if (location != -1) removeStack.removeAt(location)
    }

    fun buildDialog(context: Context) : AlertDialog.Builder {
        return AlertDialog.Builder(context)
    }
}