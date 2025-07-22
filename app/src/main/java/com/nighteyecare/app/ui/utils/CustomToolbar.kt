package com.nighteyecare.app.ui.utils

import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.nighteyecare.app.R

class CustomToolbar(private val toolbarView: View) {

    private val backButton: ImageButton = toolbarView.findViewById(R.id.toolbar_back_button)
    private val titleTextView: TextView = toolbarView.findViewById(R.id.toolbar_title)
    private val rightIconsContainer: LinearLayout = toolbarView.findViewById(R.id.toolbar_right_icons_container)

    fun setTitle(title: String) {
        titleTextView.text = title
    }

    fun setBackButtonClickListener(listener: () -> Unit) {
        backButton.setOnClickListener { listener() }
    }

    fun showBackButton(show: Boolean) {
        backButton.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun getRightIconsContainer(): LinearLayout {
        return rightIconsContainer
    }
}
