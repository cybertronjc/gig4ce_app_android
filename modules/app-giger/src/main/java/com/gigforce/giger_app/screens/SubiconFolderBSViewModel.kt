package com.gigforce.giger_app.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.giger_app.repo.IFeatureIconsDataRepository
import com.gigforce.giger_app.repo.IMainNavDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubiconFolderBSViewModel  @Inject constructor(private val repository: IFeatureIconsDataRepository) : ViewModel()  {

    var allIconsLiveData = repository.getData()


}