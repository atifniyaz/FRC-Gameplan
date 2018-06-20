package com.techhounds.frcgameplan.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import com.techhounds.frcgameplan.R
import com.techhounds.frcgameplan.adapter.FileListAdapter
import com.techhounds.frcgameplan.adapter.MenuAdapter
import com.techhounds.frcgameplan.listener.ObjectOnTouchListener
import com.techhounds.frcgameplan.model.FieldElement
import com.techhounds.frcgameplan.ui.MenuItemView
import com.techhounds.frcgameplan.ui.NavBottomView
import com.techhounds.frcgameplan.ui.PaintView
import com.techhounds.frcgameplan.ui.TeamView
import com.techhounds.frcgameplan.util.AppUtils
import com.techhounds.frcgameplan.util.SaveRestoreManager
import com.techhounds.frcgameplan.util.TutorialManager
import kotlinx.android.synthetic.main.activity_whiteboard.*
import kotlinx.android.synthetic.main.view_object.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.nio.file.Files.isDirectory
import android.R.attr.path
import android.view.MotionEvent
import android.widget.*
import kotlinx.android.synthetic.main.item_file_list.view.*
import kotlinx.android.synthetic.main.view_io_list.view.*
import java.io.File
import java.util.*


class WhiteboardActivity : AppCompatActivity(), View.OnClickListener {

    private var objectList = ArrayList<TeamView>()
    private var comment = ""
    private var launchTutorial = false

    private var windowWidth : Int? = null
    private var windowHeight : Int? = null

    private var rand = Random()

    companion object {
        fun createIntent(context : Context) : Intent {
            return Intent(context, WhiteboardActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whiteboard)

        windowHeight = resources.displayMetrics.heightPixels
        windowWidth = resources.displayMetrics.widthPixels

        setupLeftMenu()
        setupRightMenu()

        var prefs = getPreferences(Context.MODE_PRIVATE)
        var initialLaunch = prefs.getString("initial_launch", null)

        if(initialLaunch == null) {
            prefs.edit().putString("initial_launch", "no").apply()
            launchTutorial = true
        } else {
            Handler().postDelayed({
                setMenuVisibility(View.GONE)
            }, 1500)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if(launchTutorial) {
            launchTutorial()
            launchTutorial = false
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        read("")
    }

    override fun onStop() {
        write("")
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    fun write(path : String) {
        var outputStream = SaveRestoreManager.instance.openOutputFile(this, path, SaveRestoreManager.objectFileName)
        SaveRestoreManager.instance.writeObjects(objectList, outputStream)
        outputStream.close()

        outputStream = SaveRestoreManager.instance.openOutputFile(this, path, SaveRestoreManager.objectTypeFileName)
        (whiteboardBottomMenu as NavBottomView).write(outputStream)
        outputStream.close()

        outputStream = SaveRestoreManager.instance.openOutputFile(this, path, SaveRestoreManager.commentFileName)
        outputStream.writeObject(comment)
        outputStream.close()

        outputStream = SaveRestoreManager.instance.openOutputFile(this, path, SaveRestoreManager.canvasFileName)
        whiteboardMarker.write(outputStream)
        outputStream.close()

        outputStream = SaveRestoreManager.instance.openOutputFile(this, path, SaveRestoreManager.restoreStackFileName)
        outputStream.writeObject(AppUtils.instance.removeStack)
        outputStream.close()
    }

    fun read(path : String) {
        var inputStream = SaveRestoreManager.instance.openInputFile(this, path, SaveRestoreManager.objectFileName)
        if (inputStream != null) {
            var data = SaveRestoreManager.instance.readObjects(inputStream)
            inputStream.close()
            parseObjects(data)

            inputStream = SaveRestoreManager.instance.openInputFile(this, path, SaveRestoreManager.objectTypeFileName)
            if (inputStream != null) {
                (whiteboardBottomMenu as NavBottomView).read(inputStream)
                inputStream.close()
            }
        }
        inputStream = SaveRestoreManager.instance.openInputFile(this, path, SaveRestoreManager.commentFileName)
        if (inputStream != null) {
            comment = inputStream.readObject() as String
            inputStream.close()
        }
        inputStream = SaveRestoreManager.instance.openInputFile(this, path, SaveRestoreManager.canvasFileName)
        if (inputStream != null) {
            whiteboardMarker.read(inputStream)
            inputStream.close()
        }
        inputStream = SaveRestoreManager.instance.openInputFile(this, path, SaveRestoreManager.restoreStackFileName)
        if (inputStream != null) {
            AppUtils.instance.removeStack = inputStream.readObject() as ArrayList<Int>
            inputStream.close()
        }
        EventBus.getDefault().post(SetColorEvent())
    }

    fun parseObjects(data : ArrayList<String>) {
        for(datum in data) {
            var elements = datum.split(",")
            var teamObject = if(elements[2] == "object") {
                addFieldObject()
            } else {
                var data = addTeamShape(elements[2])
                data
            }
            teamObject.read(elements[0].toFloat(), elements[1].toFloat(), elements[2])
        }
    }

    private fun setupLeftMenu() {
        var listDrawable = listOf(
                R.drawable.object_box,
                R.drawable.ic_menu_paint_tool,
                R.drawable.ic_menu_comment /*,
               R.drawable.ic_menu_settings*/)
        var listId = listOf(
                R.id.menu_object_box,
                R.id.menu_paint_tool,
                R.id.menu_comment
        )
        whiteboardLeftMenu.adapter = MenuAdapter(this, listDrawable, listId, this)
        whiteboardLeftMenu.layoutManager = LinearLayoutManager(this)
    }

    private fun setupRightMenu() {
        var listDrawable = listOf(
                R.drawable.ic_menu_undo,
                R.drawable.ic_menu_reset,
                R.drawable.ic_menu_save,
                R.drawable.ic_menu_load,
                R.drawable.ic_menu_help,
                R.drawable.ic_menu_info)
        var listId = listOf(
                R.id.menu_undo,
                R.id.menu_reset,
                R.id.menu_save,
                R.id.menu_load,
                R.id.menu_help,
                R.id.menu_info
        )
        whiteboardRightMenu.adapter = MenuAdapter(this, listDrawable, listId, this)
        whiteboardRightMenu.layoutManager = LinearLayoutManager(this)
    }

    private fun undo() {
        if(objectList.size > 0) {
            whiteboard.removeView(objectList.last())
            objectList.remove(objectList.last())
        }
    }

    private fun reset() {
        while(objectList.size > 0) undo()
    }

    private fun addFieldObject() : TeamView {
        var whiteboardObject = LayoutInflater.from(this).inflate(R.layout.view_object, whiteboard, false)
        objectList.add(whiteboardObject as TeamView)
        AppUtils.instance.removeStack.add(FieldElement.FIELD_OBJECT)
        whiteboard.addView(whiteboardObject)
        whiteboardObject.setOnTouchListener(ObjectOnTouchListener())
        whiteboardObject.teamShape.setImageResource(R.drawable.object_box)
        whiteboardObject.tag = "object"
        EventBus.getDefault().post(SetColorEvent())
        maintainViewHierarchy()
        return whiteboardObject
    }

    private fun setLayoutPosition(view : View) {
        view.x = (windowWidth!! / 2 + rand.nextInt(windowWidth!! / 4) - windowWidth!! / 8 - view.layoutParams.width / 2).toFloat()
        view.y = (windowHeight!! / 2 + rand.nextInt(windowHeight!! / 4) - windowHeight!! / 8 - view.layoutParams.height / 2).toFloat()
    }

    private fun maintainViewHierarchy() {
        whiteboardRightMenu.bringToFront()
        whiteboardLeftMenu.bringToFront()
        whiteboardBottomMenu.bringToFront()
    }

    private fun setMenuVisibility(visibility : Int) {
        if (visibility == whiteboardRightMenu.visibility) { return }

        whiteboardRightMenu.visibility = visibility
        whiteboardLeftMenu.visibility = visibility
        whiteboardBottomMenu.visibility = visibility

        var leftAnimation = AnimationUtils.loadAnimation(this, if (visibility == View.VISIBLE) {
            R.anim.slide_in_left
        } else {
            R.anim.slide_out_left
        })

        var rightAnimation = AnimationUtils.loadAnimation(this, if (visibility == View.VISIBLE) {
            R.anim.slide_in_right
        } else {
            R.anim.slide_out_right
        })

        var bottomAnimation = AnimationUtils.loadAnimation(this, if (visibility == View.VISIBLE) {
            R.anim.slide_in_bottom
        } else {
            R.anim.slide_out_bottom
        })

        whiteboardLeftMenu.startAnimation(leftAnimation)
        whiteboardRightMenu.startAnimation(rightAnimation)
        whiteboardBottomMenu.startAnimation(bottomAnimation)
    }

    private fun toggleMenuVisibility() {
        var visibility = if (whiteboardRightMenu.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        setMenuVisibility(visibility)
    }

    private fun launchTutorial() {
        TutorialManager(this)
            .addWelcomeTarget()
            .buildTargets()
            .addToggleTarget()
            .addFinalTarget()
            .create().start()
    }

    override fun onClick(view: View?) {
        if(view is MenuItemView) {
            when(view.resource) {

                R.drawable.ic_menu_undo -> {
                    var stack = AppUtils.instance.removeStack

                    if(stack.size <= 0) {
                        return
                    }

                    var element = stack.removeAt(stack.size - 1)

                    if (element == FieldElement.STROKE) {
                        EventBus.getDefault().post(PaintView.UndoEvent())
                    } else if (element == FieldElement.FIELD_OBJECT) {
                        undo()
                    }

                    EventBus.getDefault().post(SetColorEvent())
                }
                R.drawable.ic_menu_reset -> {
                    invokeResetDialog()
                }
                R.drawable.ic_menu_save -> {
                    invokeSaveDialog()
                }
                R.drawable.ic_menu_load -> {
                    invokeRestoreDialog()
                }
                R.drawable.ic_menu_help -> {
                    invokeHelpDialog()
                }
                R.drawable.ic_menu_info -> {
                    invokeAboutDialog()
                }
                R.drawable.object_box -> {
                     setLayoutPosition(addFieldObject())
                }
                R.drawable.ic_menu_paint_tool -> {
                    EventBus.getDefault().post(PaintView.TogglePaintEvent(view))
                }
                R.drawable.ic_menu_comment -> {
                    invokeCommentDialog()
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onToggleMenuEvent(event: ToggleMenuEvent) {
        toggleMenuVisibility()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRemoveFieldObjectEvent(event: RemoveFieldObjectEvent){
        whiteboard.removeView(event.itemView)
        val index = objectList.indexOf(event.itemView)
        objectList.remove(event.itemView)
        AppUtils.instance.removeTypeFromStack(FieldElement.FIELD_OBJECT, index)
        EventBus.getDefault().post(SetColorEvent())
    }

    fun addTeamShape(tag : String) : TeamView {

        var whiteboardObject = LayoutInflater.from(this).inflate(R.layout.view_object, whiteboard, false)
        var size = AppUtils.instance.dpToPx(50)

        whiteboardObject.tag = tag
        whiteboardObject.teamName.visibility = View.VISIBLE
        whiteboardObject.setOnTouchListener(ObjectOnTouchListener())
        whiteboardObject.layoutParams.width = size
        whiteboardObject.layoutParams.height = size

        AppUtils.instance.removeStack.add(FieldElement.FIELD_OBJECT)
        whiteboard.addView(whiteboardObject)
        objectList.add(whiteboardObject as TeamView)

        maintainViewHierarchy()
        return whiteboardObject
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAddNewTeamShapeEvent(event : AddNewTeamShapeEvent) {
        var whiteboardObject = addTeamShape(event.tag)
        whiteboardObject.teamShape.setImageResource(event.shape)
        whiteboardObject.teamShape.setColorFilter(event.tint)
        whiteboardObject.teamName.text = event.teamNumber

        setLayoutPosition(whiteboardObject)
        EventBus.getDefault().post(SetColorEvent())
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onModifyTeamShapeEvent(event : ModifyTeamShapeEvent) {
        var color = if(event.tag.last() - '0' < 3) Color.RED else Color.BLUE
        for(i in 0 until whiteboard.childCount) {
            var child = whiteboard.getChildAt(i)
            if(child is TeamView && child.tag == event.tag) {
                child.teamShape.setImageResource(event.shape)
                child.teamShape.setColorFilter(color)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onModifyTeamNumberEvent(event : ModifyTeamNumberEvent) {
        for(i in 0 until whiteboard.childCount) {
            var child = whiteboard.getChildAt(i)
            if(child is TeamView && child.tag == event.tag) {
                child.teamName.text = event.number
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSaveEvent(event : SaveEvent) {
        write(event.path)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRestoreEvent(event : RestoreEvent) {
        reset()
        read(event.path)
    }


    private fun invokeCommentDialog() {
        var input = EditText(this)
        input.setLines(10)
        input.setText(comment, TextView.BufferType.EDITABLE)
        input.gravity = Gravity.TOP
        input.setSelection(comment.length)

        var dialogBuilder = AppUtils.instance.buildDialog(this)
                .setTitle(R.string.comments)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, { v, a ->
                    comment = input.text.toString()
                })
                .setView(input)
        dialogBuilder.create().show()
        setDialogMargin(input)
    }

    private fun invokeSaveDialog() {
        var view = LayoutInflater.from(this).inflate(R.layout.view_io_list, null)
        var dir = SaveRestoreManager.instance.getDirectories(this)

        var dialogBuilder = AppUtils.instance.buildDialog(this)
                .setTitle(R.string.save)
                .setPositiveButton(R.string.cancel, null)
                .setView(view)
        var dialog = dialogBuilder.create()

        view.file_list_recycler_view.layoutManager = LinearLayoutManager(this)
        view.file_list_recycler_view.adapter = FileListAdapter(this, dir, R.drawable.item_clear,
        View.OnClickListener { v ->
            var folder = (v.parent as LinearLayout).findViewById<TextView>(R.id.item_text).text.toString()
            var adapter = view.file_list_recycler_view.adapter as FileListAdapter

            SaveRestoreManager.instance.deleteRecursive(File(filesDir, folder))
            adapter.items = SaveRestoreManager.instance.getDirectories(this)
            adapter.notifyDataSetChanged()
        },
        View.OnClickListener{ v ->
            var text = (v as TextView).text.toString()
            EventBus.getDefault().post(WhiteboardActivity.SaveEvent(text))
            dialog.dismiss()
        })
        view.add_item.item_edit.visibility = View.VISIBLE
        view.add_item.item_text.visibility = View.GONE
        view.add_item.item_image.setImageResource(R.drawable.item_add)
        view.add_item.item_image.setOnClickListener { a ->
            var text = view.add_item.item_edit.text.toString()

            if(text.isEmpty()) {
                Toast.makeText(this, "Empty Plan Name! Did not save", Toast.LENGTH_LONG).show()
            } else {
                EventBus.getDefault().post(WhiteboardActivity.SaveEvent(text))
            }
            dialog.dismiss()
        }
        view.add_item.item_image.setColorFilter(resources.getColor(R.color.colorPrimaryDark))
        view.add_item.item_image.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                view.item_image.setColorFilter(resources.getColor(R.color.colorPrimaryDark))
            } else if (event.action == MotionEvent.ACTION_DOWN) {
                view.item_image.setColorFilter(resources.getColor(R.color.colorPrimary))
            }
            false
        }
        dialog.show()
        setDialogMargin(view)
    }

    private fun invokeRestoreDialog() {
        var view = LayoutInflater.from(this).inflate(R.layout.view_io_list, null)
        var dir = SaveRestoreManager.instance.getDirectories(this)

        if(dir.size <= 0) {
            Toast.makeText(this, "There are no saved plans!", Toast.LENGTH_LONG).show()
            return
        }

        var dialogBuilder = AppUtils.instance.buildDialog(this)
                .setTitle(R.string.restore)
                .setPositiveButton(R.string.cancel, null)
                .setView(view)
        var dialog = dialogBuilder.create()
        var onClick = View.OnClickListener { v ->
            var folder = (v.parent as LinearLayout).findViewById<TextView>(R.id.item_text).text.toString()
            EventBus.getDefault().post(RestoreEvent(folder))
            dialog.dismiss()
        }

        view.file_list_recycler_view.layoutManager = LinearLayoutManager(this)
        view.file_list_recycler_view.adapter = FileListAdapter(this, dir, R.drawable.item_whiteboard, onClick, onClick)
        view.add_item.visibility = View.GONE
        dialog.show()
        setDialogMargin(view)
    }

    private fun invokeAboutDialog() {
        var layout = LayoutInflater.from(this).inflate(R.layout.dialog_about, null)
        AppUtils.instance.buildDialog(this)
                .setTitle(R.string.about)
                .setPositiveButton(R.string.ok, null)
                .setView(layout)
                .create().show()
    }

    private fun invokeResetDialog() {
        var view = TextView(this)
        view.text = "Are you sure you want to reset the whiteboard?"

        AppUtils.instance.buildDialog(this)
                .setTitle("Reset")
                .setPositiveButton(R.string.ok, { v, a ->
                    EventBus.getDefault().post(PaintView.ResetEvent())
                    reset()
                    AppUtils.instance.clearStack()
                    setMenuVisibility(View.VISIBLE)
                    EventBus.getDefault().post(SetColorEvent())
                    (whiteboardBottomMenu as NavBottomView).reset()
                })
                .setNegativeButton(R.string.cancel, null)
                .setView(view)
                .create().show()
        setDialogTextViewMargin(view)
    }

    private fun invokeHelpDialog() {
        var layout = TextView(this)
        layout.setText(R.string.help_desc)
        AppUtils.instance.buildDialog(this)
                .setTitle(R.string.help_title)
                .setView(layout)
                .setPositiveButton(R.string.ok, { v, a -> launchTutorial() })
                .setNegativeButton(R.string.cancel, null)
                .create().show()
        setDialogTextViewMargin(layout)
    }

    private fun setDialogMargin(view : View) {
        var margin = AppUtils.instance.dpToPx(20)
        var layoutParams = view.layoutParams as FrameLayout.LayoutParams
        layoutParams.leftMargin = margin
        layoutParams.rightMargin = margin
        view.layoutParams = layoutParams
    }

    private fun setDialogTextViewMargin(view : TextView) {
        var margin = AppUtils.instance.dpToPx(24)
        var topMargin = AppUtils.instance.dpToPx(16)
        var layoutParams = view.layoutParams as FrameLayout.LayoutParams
        layoutParams.leftMargin = margin
        layoutParams.rightMargin = margin
        layoutParams.topMargin = topMargin
        view.layoutParams = layoutParams
    }

    class ToggleMenuEvent
    class RemoveFieldObjectEvent(var itemView : TeamView)
    class SetColorEvent
    class AddNewTeamShapeEvent(var tag : String, var teamNumber : String, var shape : Int, var tint : Int)
    class ModifyTeamShapeEvent(var tag : String, var shape : Int)
    class ModifyTeamNumberEvent(var tag : String, var number : String)
    class SaveEvent(var path : String)
    class RestoreEvent(var path : String)
}