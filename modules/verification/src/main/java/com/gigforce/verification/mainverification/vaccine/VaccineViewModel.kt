package com.gigforce.verification.mainverification.vaccine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.utils.Lce
import com.gigforce.verification.mainverification.vaccine.models.VaccineConfigListDM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaccineViewModel @Inject constructor(val logger: GigforceLogger): ViewModel(){

        private val _vaccineConfigLiveData = MutableLiveData<Lce<VaccineConfigListDM>>()

        val vaccineConfigLiveData : LiveData<Lce<VaccineConfigListDM>> = _vaccineConfigLiveData

        private val vaccineRepository = VaccineRepository()

        private val TAG = "Exception"



        init {

                getVaccineConfigData()

            }



        private fun getVaccineConfigData() = viewModelScope.launch{

                _vaccineConfigLiveData.value = Lce.loading()

                try {

                        _vaccineConfigLiveData.value = Lce.content(vaccineRepository.getVaccineConfigData())

                    }

                catch (e: Exception){

                        _vaccineConfigLiveData.value = Lce.error(e.toString())

                        logger.d(TAG,e.toString())

                    }

            }



    }