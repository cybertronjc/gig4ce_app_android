package com.gigforce.giger_gigs.tl_login_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

sealed class LoginSummarySharedEvents{
    object RefreshLoginSummaryList : LoginSummarySharedEvents()
}


class LoginSummarySharedViewModel : ViewModel() {

    private val _loginSummarySharedEvents = MutableLiveData<LoginSummarySharedEvents>()
    val loginSummarySharedEvents : LiveData<LoginSummarySharedEvents> = _loginSummarySharedEvents

    fun refreshLoginSummaryListInOtherScreens(){
        _loginSummarySharedEvents.value = LoginSummarySharedEvents.RefreshLoginSummaryList
    }
}