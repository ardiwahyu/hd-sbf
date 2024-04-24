package com.bm.hdsbf.utils.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.bm.hdsbf.databinding.ItemListPopupWindowBinding

class ListPopUpWindowAdapter(
    private val context: Context,
    private val listText: List<String>,
    private val selected: String?,
    private val onTypeClickListener: OnTextClickListener
): BaseAdapter() {

    interface OnTextClickListener {
        fun onClick(text: String)
    }

    override fun getCount(): Int {
        return listText.size
    }

    override fun getItem(position: Int): String {
        return listText[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val holder: ViewHolder
        var newView = view
        if (view == null) {
            holder = ViewHolder()
            val binding = ItemListPopupWindowBinding.inflate(LayoutInflater.from(context), null, false)
            holder.tvName = binding.tvText
            binding.root.tag = holder
            newView = binding.root
        } else {
            holder = newView?.tag as ViewHolder
        }

        val item = getItem(position)
        holder.tvName.text = item
        holder.tvName.setBackgroundColor(Color.parseColor(if (selected == item) "#E4F0F7" else "#FFFFFF"))
        holder.tvName.setOnClickListener {
            onTypeClickListener.onClick(item)
        }

        return newView
    }

    class ViewHolder {
        lateinit var tvName: TextView
    }
}