package com.gigforce.app.modules.preferences

import androidx.lifecycle.ViewModel
import com.gigforce.app.R

class PreferencesViewModel : ViewModel() {

    fun getPrefrencesData(): ArrayList<PreferencesItem> {
        val prefrencesItems = ArrayList<PreferencesItem>()
        prefrencesItems.add(PreferencesItem(R.drawable.ic_products,"Category",""))
        prefrencesItems.add(PreferencesItem(R.drawable.ic_product_services_pressed,"Roles","At atm"))
        prefrencesItems.add(PreferencesItem(R.drawable.ic_referal,"Day and Time","00-04days,1200-1300hrs"))
        prefrencesItems.add(PreferencesItem(R.drawable.ic_settings,"Location","Work from home,Bangalore"))
        prefrencesItems.add(PreferencesItem(R.drawable.ic_settings,"Earning","2000-2200rs"))
        prefrencesItems.add(PreferencesItem(0,"Others",""))
        prefrencesItems.add(PreferencesItem(R.drawable.ic_link_broken,"App Language","English"))
        prefrencesItems.add(PreferencesItem(R.drawable.ic_broadcast,"Notification",""))
        prefrencesItems.add(PreferencesItem(R.drawable.ic_products,"Sign Out",""))

        return prefrencesItems;
    }

}