package com.techhounds.frcgameplan.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import com.techhounds.frcgameplan.util.AppUtils
import com.techhounds.frcgameplan.R
import com.techhounds.frcgameplan.activity.WhiteboardActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MenuItemView : ImageView {

    var resource : Int? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        resource = resId
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        EventBus.getDefault().unregister(this)
        super.onDetachedFromWindow()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSetColorEvent(event: WhiteboardActivity.SetColorEvent) {
        if(resource == R.drawable.ic_menu_undo) {
            var stack = AppUtils.instance.removeStack
            if (stack.size <= 0) {
                setColorFilter(Color.GRAY)
                isClickable = false
            } else {
                setColorFilter(Color.WHITE)
                isClickable = true
            }
        }
    }
}