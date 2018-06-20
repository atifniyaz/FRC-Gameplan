package com.techhounds.frcgameplan.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.techhounds.frcgameplan.R
import com.techhounds.frcgameplan.activity.WhiteboardActivity
import com.techhounds.frcgameplan.ui.MenuItemView
import org.greenrobot.eventbus.EventBus

class MenuAdapter(var context : Context, var items : List<Int>, var itemsIds : List<Int>, val onClickListener: View.OnClickListener)
    : RecyclerView.Adapter<MenuAdapter.MenuHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MenuHolder?, position: Int) {
        var imageView = holder?.imageView
        var resource = items[position]
        imageView?.setImageResource(resource)
        imageView?.setOnClickListener(onClickListener)
        imageView?.id = itemsIds[position]

        EventBus.getDefault().post(WhiteboardActivity.SetColorEvent())
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MenuHolder {
        var imageView = LayoutInflater.from(context).inflate(R.layout.view_menu, parent, false) as MenuItemView
        return MenuHolder(imageView)
    }

    class MenuHolder(var imageView : MenuItemView) : RecyclerView.ViewHolder(imageView)
}