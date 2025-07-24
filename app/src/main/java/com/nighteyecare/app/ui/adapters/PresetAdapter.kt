package com.nighteyecare.app.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nighteyecare.app.data.model.BlueLightPreset
import com.nighteyecare.app.databinding.PresetItemBinding

class PresetAdapter(
    private val presets: List<BlueLightPreset>,
    private val onPresetSelected: (BlueLightPreset) -> Unit
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
        fun bind(preset: BlueLightPreset, isSelected: Boolean) {
            binding.presetName.text = preset.name
            binding.presetColor.setBackgroundColor(ContextCompat.getColor(itemView.context, preset.colorRes))
            itemView.isSelected = isSelected
        }
    }
}