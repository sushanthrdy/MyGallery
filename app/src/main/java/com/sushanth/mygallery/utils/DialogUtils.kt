package com.sushanth.mygallery.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

object DialogUtils {

    fun showAlertDialog(
        context: Context,
        title: String? = null,
        message: String? = null,
        positiveButtonText: String? = null,
        negativeButtonText: String? = null,
        isCancelable: Boolean = true,
        positiveButtonAction: (() -> Unit)? = null,
        negativeButtonAction: (() -> Unit)? = null,
    ) {
        val builder = AlertDialog.Builder(context)

        title?.let { builder.setTitle(it) }
        message?.let { builder.setMessage(it) }

        positiveButtonText?.let {
            builder.setPositiveButton(it) { dialog, _ ->
                positiveButtonAction?.invoke()
                dialog.dismiss()
            }
        }

        negativeButtonText?.let {
            builder.setNegativeButton(it) { dialog, _ ->
                negativeButtonAction?.invoke()
                dialog.dismiss()
            }
        }

        builder.setCancelable(isCancelable)

        val dialog = builder.create()
        dialog.show()
    }
}