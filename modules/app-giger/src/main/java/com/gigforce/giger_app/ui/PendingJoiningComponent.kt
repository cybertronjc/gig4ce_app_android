package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.common_ui.components.molecules.HorizontalScrollingPageComponent
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.viewdatamodels.PendingJoiningItemDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.crashlytics.CrashlyticsLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PendingJoiningComponent constructor(
    context: Context,
    attrs: AttributeSet?
) : HorizontalScrollingPageComponent<PendingJoiningItemDVM>(
    context = context,
    attrs = attrs,
    shouldShowScrollIndicator = true,
    shouldEnablePageSnap = true
), IViewHolder {

    @Inject
    lateinit var leadManagementRepository: LeadManagementRepository

    init {
        getPendingJoinings()
    }

    private fun getPendingJoinings() = GlobalScope.launch {
        try {
            val pendingJoinigs = leadManagementRepository.getPendingJoinings()

            this.launch(Dispatchers.Main) {
                setData(pendingJoinigs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CrashlyticsLogger.e("PendingJoiningComponent","while fetching and setting pending joining compoent",e)
        }
    }

    override fun bind(data: Any?) {

    }
}