package com.bm.hdsbf.ui.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bm.hdsbf.R
import com.bm.hdsbf.data.local.db.entities.ScheduleVo
import com.bm.hdsbf.databinding.ItemListScheduleBinding
import javax.inject.Inject

class ScheduleAdapter @Inject constructor() : ListAdapter<ScheduleVo, ScheduleAdapter.ViewHolder>(diffUtils){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListScheduleBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val item = getItem(position)
            val context = holder.binding.root.context
            holder.binding.tvType.text = item.type
            holder.binding.tvName.text = item.name
            when (item.type) {
                "HD1" -> holder.binding.tvType.background = ContextCompat.getDrawable(context, R.color.hd1)
                "HD2" -> holder.binding.tvType.background = ContextCompat.getDrawable(context, R.color.hd2)
                "HD3" -> holder.binding.tvType.background = ContextCompat.getDrawable(context, R.color.hd3)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class ViewHolder(itemBinding: ItemListScheduleBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        var binding: ItemListScheduleBinding = itemBinding
    }

    companion object {
        private val diffUtils = object : DiffUtil.ItemCallback<ScheduleVo>() {
            override fun areItemsTheSame(oldItem: ScheduleVo, newItem: ScheduleVo): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ScheduleVo, newItem: ScheduleVo): Boolean =
                oldItem.type == newItem.type
        }
    }
}