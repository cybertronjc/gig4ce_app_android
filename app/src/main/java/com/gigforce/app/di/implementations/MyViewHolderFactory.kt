package com.gigforce.app.di.implementations

import com.gigforce.core.CoreViewHolderFactory
import com.gigforce.modules.feature_chat.ChatViewTypeLoader
import javax.inject.Inject

class MyViewHolderFactory @Inject constructor(): CoreViewHolderFactory() {

    override fun registerAllViewTypeLoaders() {
        this.registerViewTypeLoader(ChatViewTypeLoader())
    }
}