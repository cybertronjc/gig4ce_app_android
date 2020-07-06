package com.gigforce.app.modules.gigPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.gigPage.models.Gig

class GigViewModel constructor(
    private val gigRepository: GigRepository = GigRepository()
) : ViewModel() {

    private val _gigDetails = MutableLiveData<Gig>()
    val gigDetails: LiveData<Gig> get() = _gigDetails

    fun getPresentGig(gigId: String) {

    }
}