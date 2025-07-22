package com.nighteyecare.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nighteyecare.app.databinding.LanguageItemBinding

class LanguageAdapter(
    private val languages: List<Pair<String, String>>,
    private val onLanguageSelected: (String) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var selectedLanguageCode: String? = null

    fun setSelectedLanguage(languageCode: String) {
        selectedLanguageCode = languageCode
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = LanguageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val (languageName, languageCode) = languages[position]
        holder.bind(languageName, languageCode == selectedLanguageCode)
        holder.itemView.setOnClickListener {
            onLanguageSelected(languageCode)
        }
    }

    override fun getItemCount() = languages.size

    inner class LanguageViewHolder(private val binding: LanguageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(languageName: String, isSelected: Boolean) {
            binding.languageName.text = languageName
            // TODO: Set flag image based on languageCode
            binding.languageSelectedCheckmark.visibility = if (isSelected) View.VISIBLE else View.GONE
        }
    }
}
