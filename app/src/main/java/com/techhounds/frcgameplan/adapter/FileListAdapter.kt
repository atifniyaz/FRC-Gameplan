package com.techhounds.frcgameplan.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.techhounds.frcgameplan.R
import kotlinx.android.synthetic.main.item_file_list.view.*


class FileListAdapter(var context : Context, var items : List<String>, var imgRes : Int, var imageTouch : View.OnClickListener, var textTouch : View.OnClickListener)
    : RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        var view = holder!!.view
        view.item_image.setImageResource(imgRes)
        view.item_image.setOnClickListener(imageTouch)
        view.item_text.setOnClickListener(textTouch)

        view.item_image.setColorFilter(context.resources.getColor(R.color.colorPrimaryDark))
        view.item_text.text = items[position]
        view.item_text.setTextColor(context.resources.getColor(R.color.colorPrimaryDark))
        view.item_image.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                view.item_image.setColorFilter(context.resources.getColor(R.color.colorPrimaryDark))
            } else if (event.action == MotionEvent.ACTION_DOWN) {
                view.item_image.setColorFilter(context.resources.getColor(R.color.colorPrimary))
            }
            false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        var imageView = LayoutInflater.from(context).inflate(R.layout.item_file_list, parent, false)
        return ViewHolder(imageView)
    }

    class ViewHolder(var view : View) : RecyclerView.ViewHolder(view)
}