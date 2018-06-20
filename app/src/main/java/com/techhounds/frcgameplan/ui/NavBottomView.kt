package com.techhounds.frcgameplan.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.text.InputType
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.techhounds.frcgameplan.R
import com.techhounds.frcgameplan.activity.WhiteboardActivity
import com.techhounds.frcgameplan.util.AppUtils
import kotlinx.android.synthetic.main.view_object.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class NavBottomView : LinearLayout {

    val teamHeading = mutableListOf(1, 2, 3, 3, 2, 1)
    var teamNumbers = mutableListOf(-1, -1, -1, -1, -1, -1)
    var teamRes = mutableListOf(R.drawable.object_circle, R.drawable.object_square, R.drawable.object_octagon,
            R.drawable.object_circle, R.drawable.object_square, R.drawable.object_octagon)
    var teamIds = listOf(R.id.red_one, R.id.red_two, R.id.red_three, R.id.blue_three, R.id.blue_two, R.id.blue_one)
    val teamTags = listOf("team_shape_0", "team_shape_1", "team_shape_2", "team_shape_3", "team_shape_4", "team_shape_5")
    val cycle = mutableListOf(R.drawable.object_circle, R.drawable.object_square, R.drawable.object_octagon)

    private val MAX_LENGTH = 5

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onFinishInflate() {
        super.onFinishInflate()
        reset()
    }

    fun reset() {
        var padding = AppUtils.instance.dpToPx(8)
        var shapeHeight = AppUtils.instance.dpToPx(60)

        for(i in 0 until childCount) {
            var item = getChildAt(i) as TeamView

            item.teamUnderline.visibility = View.VISIBLE
            item.teamShape.setImageResource(teamRes[i])
            item.teamShape.layoutParams = item.teamShape.layoutParams.apply {
                width = LayoutParams.MATCH_PARENT
                height = shapeHeight
            }
            item.teamShape.visibility = View.VISIBLE
            item.teamName.visibility = View.VISIBLE
            item.teamId.visibility = View.VISIBLE
            item.teamId.text = teamHeading[i].toString()

            item.setPadding(padding, padding, padding, padding)
            item.teamName.layoutParams.width = item.teamShape.layoutParams.width
            item.teamName.layoutParams.height = item.teamName.layoutParams.width

            if(i < 3) {
                item.teamUnderline.setBackgroundColor(Color.RED)
                item.setBackgroundColor(0xFF000000.toInt())
                item.teamShape.setColorFilter(Color.RED)
                item.teamId.setTextColor(Color.RED)
            } else {
                item.teamUnderline.setBackgroundColor(Color.BLUE)
                item.setBackgroundColor(0xFF000000.toInt())
                item.teamShape.setColorFilter(Color.BLUE)
                item.teamId.setTextColor(Color.BLUE)
            }

            item.imageResourceIndex = i % 3
            item.teamShape.setImageResource(cycle[item.imageResourceIndex!!])
            item.teamName.text = ""

            teamNumbers = mutableListOf(-1, -1, -1, -1, -1, -1)
            teamRes = mutableListOf(R.drawable.object_circle, R.drawable.object_square, R.drawable.object_octagon,
                    R.drawable.object_circle, R.drawable.object_square, R.drawable.object_octagon)

            item.teamShape.setOnClickListener {
                EventBus.getDefault().post(WhiteboardActivity.AddNewTeamShapeEvent("team_shape_" + i.toString(),
                        getStringForNumber(teamNumbers[i]), cycle[item.imageResourceIndex!!], if (i < 3) Color.RED else Color.BLUE))
            }
            item.teamShape.setOnLongClickListener {
                item.imageResourceIndex = (item.imageResourceIndex!! + 1) % 3
                var res = cycle[item.imageResourceIndex!!]
                teamRes[i] = res
                item.teamShape.setImageResource(res)

                EventBus.getDefault().post(WhiteboardActivity.ModifyTeamShapeEvent("team_shape_" + i.toString(), cycle[item.imageResourceIndex!!]))
                true
            }
            item.tag = "team_shape_" + i.toString()
            item.id = teamIds[i]
            item.teamId.setOnLongClickListener {
                invokeSetTeamNumberDialog(teamHeading[i], i < 3, i)
                true
            }
        }
    }

    private fun invokeSetTeamNumberDialog(number : Int, isRed : Boolean, index : Int) {
        var color = if (isRed) "Red" else "Blue"

        var margin = AppUtils.instance.dpToPx(20)
        var tag = "team_shape_" + index.toString()
        var input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setRawInputType(Configuration.KEYBOARD_12KEY)

        var dialogBuilder = AppUtils.instance.buildDialog(context)
                .setTitle("Set Team Number for " + color + " " + number.toString())
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", { v, a ->
                    validateNumber(input.text.toString(), index)
                    EventBus.getDefault().post(WhiteboardActivity.ModifyTeamNumberEvent(tag, getStringForNumber(teamNumbers[index])))
                })
                .setView(input)
        dialogBuilder.create().show()

        var layoutParams = input.layoutParams as FrameLayout.LayoutParams
        layoutParams.leftMargin = margin
        layoutParams.rightMargin = margin
        input.layoutParams = layoutParams
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onModifyTeamNumberEvent(event : WhiteboardActivity.ModifyTeamNumberEvent) {
        findViewWithTag<TeamView>(event.tag).teamName.text = event.number
    }

    fun validateNumber(teamNumber : String, index : Int) {
        if(TextUtils.isEmpty(teamNumber) || teamNumber.length > MAX_LENGTH) {
            teamNumbers[index] = -1
        } else {
            var number = teamNumber.toInt()
            if(number < 0) {
                teamNumbers[index] = -1
            } else {
                teamNumbers[index] = number
            }
        }
    }

    fun getStringForNumber(number : Int) : String {
        if(number == -1) {
            return ""
        } else {
            return number.toString()
        }
    }

    fun write(stream : ObjectOutputStream) {
        var list = ArrayList<Int>()
        for(i in 0 until teamRes.size) {
            list.add(cycle.indexOf(teamRes[i]))
        }
        stream.writeObject(list)
        stream.writeObject(teamNumbers)
    }

    fun read(inputStream: ObjectInputStream) {
        var listRes = inputStream.readObject() as ArrayList<Int>
        teamNumbers = inputStream.readObject() as ArrayList<Int>
        for(i in 0 until listRes.size) {
            teamRes[i] = cycle.elementAt(listRes[i])
        }
        for(i in 0 until teamTags.size) {
            EventBus.getDefault().post(WhiteboardActivity.ModifyTeamShapeEvent(teamTags[i], teamRes[i]))
            EventBus.getDefault().post(WhiteboardActivity.ModifyTeamNumberEvent(teamTags[i], getStringForNumber(teamNumbers[i])))
        }
        for(i in 0 until childCount) {
            var item = getChildAt(i) as TeamView
            item.imageResourceIndex = listRes[i]
            item.teamShape.setImageResource(teamRes[i])
            item.teamName.text = getStringForNumber(teamNumbers[i])
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        EventBus.getDefault().unregister(this)
        super.onDetachedFromWindow()
    }
}