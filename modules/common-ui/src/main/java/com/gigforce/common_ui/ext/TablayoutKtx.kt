package com.gigforce.common_ui.ext

import com.google.android.material.tabs.TabLayout


inline fun TabLayout.onTabSelected(
    crossinline action: (
        tab: TabLayout.Tab?
    ) -> Unit
): TabLayout.OnTabSelectedListener = addTabChangedListener(onTabSelected = action)


inline fun TabLayout.addTabChangedListener(
    crossinline onTabSelected: (
        tab: TabLayout.Tab?
    ) -> Unit = { _ -> },
    crossinline onTabUnselected: (
        tab: TabLayout.Tab?
    ) -> Unit = { _ -> },
    crossinline onTabReselected: (
        tab: TabLayout.Tab?
    ) -> Unit = { _ -> }
): TabLayout.OnTabSelectedListener {
    val tabChangeWatcher = object : TabLayout.OnTabSelectedListener {

        override fun onTabSelected(tab: TabLayout.Tab?) {
            onTabSelected.invoke(tab)
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            onTabUnselected.invoke(tab)
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
            onTabReselected.invoke(tab)
        }
    }
    addOnTabSelectedListener(tabChangeWatcher)
    return tabChangeWatcher
}