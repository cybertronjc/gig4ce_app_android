package com.gigforce.lead_management.ui.select_team_leader

import com.gigforce.common_ui.viewdatamodels.leadManagement.TeamLeader

object TeamLeadersDataCache {

    private var allTeamLeaders : List<TeamLeader>? = null
    private var cityTeamLeaders : List<TeamLeader>? = null

    fun isAllTeamLeadersCached() = allTeamLeaders != null

    fun isCityTeamLeadersCached() = cityTeamLeaders != null

    fun getCachedAllTeamLeadersIfExistElseThrow() : List<TeamLeader>{
        return allTeamLeaders ?: throw IllegalStateException("no cached team leader present")
    }

    fun getCityTeamLeadersIfExistElseThrow() : List<TeamLeader>{
        return cityTeamLeaders ?: throw IllegalStateException("no cached team leader present")
    }

    fun updateAllCachedTeamLeader(
        allTeamLeaders : List<TeamLeader>
    ){
        this.allTeamLeaders = allTeamLeaders
    }

    fun updateCityTeamLeader(
        cityTeamLeaders : List<TeamLeader>
    ){
        this.cityTeamLeaders = cityTeamLeaders
    }

    fun clearAllCachedTeamLeaders(){
        cityTeamLeaders = null
        allTeamLeaders = null
    }
}