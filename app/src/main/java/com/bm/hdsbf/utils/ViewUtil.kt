package com.bm.hdsbf.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import com.bm.hdsbf.R
import com.bm.hdsbf.databinding.DialogConfirmBinding
import com.bm.hdsbf.databinding.DialogErrorBinding
import com.bm.hdsbf.databinding.DialogLoadingBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

object ViewUtil {
    fun View.setVisible() {
        this.visibility = View.VISIBLE
    }

    fun View.setInvisible() {
        this.visibility = View.INVISIBLE
    }

    fun View.setGone() {
        this.visibility = View.GONE
    }

    fun Context.showShortToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun Context.showLongToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun Context.dialogError(title: String, content: String, titleButton: String, cancelable: Boolean, onclickBtn: (dialog: Dialog) -> Unit): Dialog {
        val dialog = BottomSheetDialog(this)
        val binding = DialogErrorBinding.inflate(LayoutInflater.from(this), null, false)
        dialog.setContentView(binding.root)
        binding.tvTitle.text = title
        binding.tvContent.text = content
        dialog.setCancelable(cancelable)
        binding.btnOk.text = titleButton
        binding.btnOk.setOnClickListener { onclickBtn.invoke(dialog) }
        return dialog
    }

    fun Context.dialogConfirm(title: String, content: String, titleButton: String, titleButton2: String, onclickBtn: (dialog: Dialog) -> Unit, onclickBtn2: (dialog: Dialog) -> Unit, cancelable: Boolean,): Dialog {
        val dialog = BottomSheetDialog(this)
        val binding = DialogConfirmBinding.inflate(LayoutInflater.from(this), null, false)
        dialog.setContentView(binding.root)
        binding.tvTitle.text = title
        binding.tvContent.text = content
        dialog.setCancelable(cancelable)
        binding.btnOk.text = titleButton
        binding.btnOk.setOnClickListener { onclickBtn.invoke(dialog) }
        binding.btnNotOk.text = titleButton2
        binding.btnNotOk.setOnClickListener { onclickBtn2.invoke(dialog) }
        return dialog
    }

    fun dialogLoading(context: Context): Dialog {
        val dialog = Dialog(context, R.style.MaterialDialogSheet)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val bindingDialog = DialogLoadingBinding.inflate(LayoutInflater.from(context), null, false)
        dialog.setContentView(bindingDialog.root)
        dialog.setCancelable(false)
        return dialog
    }
}