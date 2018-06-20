package com.techhounds.frcgameplan.util

import android.app.Activity
import android.graphics.PointF
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.target.Target
import com.takusemba.spotlight.target.SimpleTarget
import com.takusemba.spotlight.OnTargetStateChangedListener
import com.takusemba.spotlight.shape.Circle
import com.techhounds.frcgameplan.R

class TutorialManager(var activity: Activity) {

    private var targets = ArrayList<Target>()

    class TargetCandidate(var viewId : Int, var title : Int, var description : Int,
                          var listener : OnTargetStateChangedListener<SimpleTarget>?)

    private var targetCandidates = mutableListOf(
            TargetCandidate(R.id.whiteboardLeftMenu, R.string.tutorial_left_menu_title, R.string.tutorial_left_menu_desc, null),
            TargetCandidate(R.id.menu_object_box, R.string.tutorial_game_piece_title, R.string.tutorial_game_piece_desc, null),
            TargetCandidate(R.id.menu_paint_tool, R.string.tutorial_pen_color_title, R.string.tutorial_pen_color_desc, null),
            TargetCandidate(R.id.menu_comment, R.string.tutorial_notes_title, R.string.tutorial_notes_desc, null),

            TargetCandidate(R.id.whiteboardBottomMenu, R.string.tutorial_bottom_menu_title, R.string.tutorial_bottom_menu_desc, null),
            TargetCandidate(R.id.red_three, R.string.tutorial_bottom_item_title, R.string.tutorial_bottom_item_desc, null),

            TargetCandidate(R.id.whiteboardRightMenu, R.string.tutorial_right_menu_title, R.string.tutorial_right_menu_desc, null),
            TargetCandidate(R.id.menu_undo, R.string.tutorial_undo_title, R.string.tutorial_undo_desc, null),
            TargetCandidate(R.id.menu_reset, R.string.tutorial_reset_title, R.string.tutorial_reset_desc, null),
            TargetCandidate(R.id.menu_help, R.string.tutorial_help_title, R.string.tutorial_help_desc, null)
    )

    fun create() : Spotlight {
        return Spotlight.with(activity)
                .setOverlayColor(R.color.tutorialBackground)
                .setClosedOnTouchedOutside(true)
                .setAnimation(DecelerateInterpolator(2f))
                .setTargets(targets)
    }

    fun addTarget(target : Target) {
        targets.add(target)
    }

    fun buildTargets() : TutorialManager {
        for(targetCandidate in targetCandidates) {
            addTarget(targetCandidate)
        }
        return this
    }

    fun addTarget(target : TargetCandidate) {
        val dpPadding = AppUtils.instance.dpToPx(2) + 0f
        val center = getCenterPoint(activity.findViewById<View>(target.viewId))
        val simpleTarget = SimpleTarget.Builder(activity)
                .setPoint(center.x, center.y)
            .setShape(Circle(getTargetSize(activity.findViewById<View>(target.viewId)) + dpPadding))
            .setTitle(activity.getString(target.title))
            .setDescription(activity.getString(target.description))
            if(target.listener != null) {
                simpleTarget.setOnSpotlightStartedListener(target.listener!!)
            }
        addTarget(simpleTarget.build())
    }

    fun addWelcomeTarget() : TutorialManager {
        var simpleTarget = SimpleTarget.Builder(activity)
                .setTitle(activity.getString(R.string.tutorial_init_title))
                .setShape(Circle(0f))
                .setDescription(activity.getString(R.string.tutorial_init_desc))
                .build()
        addTarget(simpleTarget)
        return this
    }

    fun addToggleTarget() : TutorialManager {
        var simpleTarget = SimpleTarget.Builder(activity)
                .setTitle(activity.getString(R.string.tutorial_menu_title))
                .setShape(Circle(0f))
                .setDescription(activity.getString(R.string.tutorial_menu_desc))
                .build()
        addTarget(simpleTarget)
        return this
    }

    fun addFinalTarget() : TutorialManager {
        var simpleTarget = SimpleTarget.Builder(activity)
                .setTitle(activity.getString(R.string.tutorial_final_title))
                .setShape(Circle(0f))
                .setDescription(activity.getString(R.string.tutorial_final_desc))
                .build()
        addTarget(simpleTarget)
        return this
    }

    private fun getCenterPoint(view : View) : PointF {
        var array = intArrayOf(0, 0)
        view.getLocationOnScreen(array)
        return PointF((array[0] + view.width / 2).toFloat(), (array[1] + view.height / 2).toFloat())
    }

    private fun getTargetSize(view : View) : Float {
        return if(view.height > view.width) {
            view.height + 0f
        } else {
            view.width + 0f
        }
    }

    fun clearTargets() {
        targets.clear()
    }
}