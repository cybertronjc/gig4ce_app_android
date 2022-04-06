package com.gigforce.giger_app.help

import javax.inject.Inject


class HelpSectionRepository @Inject constructor(val helpSectionService: HelpSectionService) {
    suspend fun getHelpSectionData(): ArrayList<HelpSectionDM> {
        val responseData = helpSectionService.getHelpSectionData()
        if (responseData.isSuccessful) {
            responseData.body()?.data?.let {
                return ArrayList(it)
            } ?: run {
                ArrayList<HelpSectionDM>()
            }
        }
        return ArrayList<HelpSectionDM>()
    }
}