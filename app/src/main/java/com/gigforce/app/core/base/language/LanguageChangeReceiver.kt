package com.gigforce.app.core.base.language

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class LanguageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(contxt: Context?, intent: Intent?) {
        Toast.makeText(contxt,"working",Toast.LENGTH_LONG).show()
    }
}