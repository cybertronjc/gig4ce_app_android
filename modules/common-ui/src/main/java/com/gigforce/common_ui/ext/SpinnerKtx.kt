package com.gigforce.common_ui.ext

import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner


inline fun AppCompatSpinner.onItemSelected(
        crossinline action: (
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
        ) -> Unit
): AdapterView.OnItemSelectedListener = addOnItemSelectedListener(onItemSelected = action)


inline fun AppCompatSpinner.addOnItemSelectedListener(
        crossinline onItemSelected: (
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
        ) -> Unit = { _, _, _, _ -> },
        crossinline onNothingSelected: (
                parent: AdapterView<*>?
        ) -> Unit = { _ -> }
): AdapterView.OnItemSelectedListener {
    val itemSelectedListener = object : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            onItemSelected.invoke(parent, view, position, id)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            onNothingSelected.invoke(parent)
        }
    }

    onItemSelectedListener = itemSelectedListener
    return itemSelectedListener
}