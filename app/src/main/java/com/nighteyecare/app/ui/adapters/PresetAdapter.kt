package com.nighteyecare.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nighteyecare.app.databinding.PresetItemBinding

class PresetAdapter(
    private val presets: List<String>,
    private val onPresetSelected: (String) -> Unit
) : RecyclerView.Adapter<PresetAdapter.PresetViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetViewHolder {
        val binding = PresetItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PresetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int) {
        holder.bind(presets[position], position == selectedPosition)
        holder.itemView.setOnClickListener {
            selectedPosition = holder.adapterPosition
            onPresetSelected(presets[selectedPosition])
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = presets.size

    inner class PresetViewHolder(private val binding: PresetItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(presetName: String, isSelected: Boolean) {
            binding.presetName.text = presetName
            itemView.isSelected = isSelected
        }
    }
}
