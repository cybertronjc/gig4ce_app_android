package com.gigforce.lead_management.ui.drop_selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.core.utils.Lse

class DropSelectionViewModel : ViewModel() {

    private val _viewState : MutableLiveData<Lse> = MutableLiveData()
    private val viewState : LiveData<Lse> = _viewState


}