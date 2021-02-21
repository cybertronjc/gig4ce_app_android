package com.gigforce.common_ui.ext

import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.showToast(string: String){
    Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
}