package com.gigforce.client_activation.client_activation.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.extensions.getOrThrow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class BLocationHubViewModel : ViewModel() {
    val _states = MutableLiveData<List<String>>()
    val states : LiveData<List<String>> = _states

    val _hub = MutableLiveData<List<String>>()
    val hub : LiveData<List<String>> = _hub

    val _allBusinessLocactionsList = MutableLiveData<List<BusinessLocationDM>>()
    val allBusinessLocactionsList : LiveData<List<BusinessLocationDM>> = _allBusinessLocactionsList

    fun loadStates(businessId : String)= viewModelScope.launch{
        var businessLocactionsSnapshot = FirebaseFirestore.getInstance().collection("Business_Locations").whereEqualTo("business_id","pYCDZ5hqfPPMOYuyCjtt").whereEqualTo("type","office").getOrThrow()
        val businessLocactions = arrayListOf<BusinessLocationDM>()
        businessLocactionsSnapshot?.let { querySnapshot ->
            querySnapshot.forEach{ snaphot->
                var model = snaphot.toObject(BusinessLocationDM::class.java)
                model.id = snaphot.id
                businessLocactions.add(model)
            }
        }
        _allBusinessLocactionsList.value = businessLocactions
        var bLocationsList = arrayListOf<String>()
        businessLocactions.forEach{
            it.state?.name?.let {
                bLocationsList.add(it)
            }
        }
        _states.value = bLocationsList.distinct()
    }

    fun loadHub(state : String){
        var filteredHub = allBusinessLocactionsList.value?.filter { it.state?.name == state }
        var hubList = arrayListOf<String>()
        filteredHub?.forEach{
            it.name?.let {
                hubList.add(it)

            }
        }
        _hub.value = hubList
    }

    fun getHubDocId(state : String,hub:String):String?{
       return _allBusinessLocactionsList.value?.filter { it.state?.name == state && it.name == hub }?.get(0)?.id
    }

    fun saveHubLocationData(state : String,hub:String){

    }
}