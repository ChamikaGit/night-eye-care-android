package com.nighteyecare.app.ui.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.nighteyecare.app.R

class CustomAlertDialog(context: Context) {

    private val dialog = Dialog(context)

    init {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun showMessageDialog(
        title: String,
        message: String,
        positiveButtonText: String = "OK",
        onPositiveClick: (() -> Unit)? = null
    ) {
        dialog.setContentView(R.layout.custom_alert_dialog)

        val dialogTitle = dialog.findViewById<TextView>(R.id.dialog_title)
        val dialogMessage = dialog.findViewById<TextView>(R.id.dialog_message)
        val positiveButton = dialog.findViewById<Button>(R.id.dialog_positive_button)
        val negativeButton = dialog.findViewById<Button>(R.id.dialog_negative_button)
        val buttonContainer = dialog.findViewById<LinearLayout>(R.id.dialog_button_container)

        dialogTitle.text = title
        dialogMessage.text = message
        positiveButton.text = positiveButtonText

        negativeButton.visibility = View.GONE // Hide negative button for message dialog

        positiveButton.setOnClickListener {
            dialog.dismiss()
            onPositiveClick?.invoke()
        }
        dialog.show()
    }

    fun showConfirmationDialog(
        title: String,
        message: String,
        positiveButtonText: String = "OK",
        negativeButtonText: String = "Cancel",
        onPositiveClick: (() -> Unit)? = null,
        onNegativeClick: (() -> Unit)? = null
    ) {
        dialog.setContentView(R.layout.custom_alert_dialog)

        val dialogTitle = dialog.findViewById<TextView>(R.id.dialog_title)
        val dialogMessage = dialog.findViewById<TextView>(R.id.dialog_message)
        val positiveButton = dialog.findViewById<Button>(R.id.dialog_positive_button)
        val negativeButton = dialog.findViewById<Button>(R.id.dialog_negative_button)
        val buttonContainer = dialog.findViewById<LinearLayout>(R.id.dialog_button_container)

        dialogTitle.text = title
        dialogMessage.text = message
        positiveButton.text = positiveButtonText
        negativeButton.text = negativeButtonText

        negativeButton.visibility = View.VISIBLE // Show negative button for confirmation dialog

        positiveButton.setOnClickListener {
            dialog.dismiss()
            onPositiveClick?.invoke()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
            onNegativeClick?.invoke()
        }
        dialog.show()
    }
}