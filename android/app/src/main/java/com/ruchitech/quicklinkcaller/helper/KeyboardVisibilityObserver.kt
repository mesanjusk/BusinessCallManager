package com.ruchitech.quicklinkcaller.helper

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewTreeObserver

class KeyboardVisibilityObserver(private val context: Context, private val onKeyboardVisibilityChanged: (Boolean) -> Unit) {

    private var rootView: View? = null
    private var isKeyboardVisible = false

    fun start() {
        rootView = (context as? Activity)?.window?.decorView?.rootView

        rootView?.viewTreeObserver?.addOnGlobalLayoutListener(globalLayoutListener)
    }

    fun stop() {
        rootView?.viewTreeObserver?.removeOnGlobalLayoutListener(globalLayoutListener)
        rootView = null
    }

    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val heightDiff = rootView!!.rootView.height - rootView!!.height
        val isVisible = heightDiff > 100 // Adjust this threshold as needed

        if (isVisible != isKeyboardVisible) {
            isKeyboardVisible = isVisible
            onKeyboardVisibilityChanged(isVisible)
        }
    }
}
