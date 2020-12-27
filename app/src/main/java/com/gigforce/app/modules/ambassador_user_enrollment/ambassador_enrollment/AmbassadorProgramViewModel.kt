package com.gigforce.app.modules.ambassador_user_enrollment.ambassador_enrollment

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.ambassador_user_enrollment.models.AmbassadorApplication
import com.gigforce.app.modules.ambassador_user_enrollment.models.AmbassadorProfiles
import com.gigforce.app.modules.client_activation.ApplicationClientActivationRepository
import com.gigforce.app.modules.client_activation.models.JpSettings
import com.gigforce.app.utils.SingleLiveEvent
import com.gigforce.app.utils.lang_models.LangMapSingleton
import java.util.*

class AmbassadorProgramViewModel : ViewModel() {
    val repository = RepositoryAmbassadorProgram()


    private val _observableAmbassadorProgram: SingleLiveEvent<AmbassadorProfiles> by lazy {
        SingleLiveEvent<AmbassadorProfiles>();
    }
    val observableAmbassadorProgram: SingleLiveEvent<AmbassadorProfiles> get() = _observableAmbassadorProgram

    private val _observableAmbassadorApplication: SingleLiveEvent<AmbassadorApplication> by lazy {
        SingleLiveEvent<AmbassadorApplication>();
    }
    val observableAmbassadorApplication: SingleLiveEvent<AmbassadorApplication> get() = _observableAmbassadorApplication

    fun getAmbassadorProfiles() {

        repository.getCollectionReference().whereEqualTo("type", "ambassador_program")
            .addSnapshotListener { success, err ->
                if (!success?.documents.isNullOrEmpty()) {
                    run {
                        if (Locale.getDefault().displayName != "en") {
                            repository.db.collection("Configuration")
                                .document("AmbassadorTranslations")
                                .addSnapshotListener { success_, err_ ->
                                    run {
                                        LangMapSingleton.langMap =
                                            success_?.data?.get(Locale.getDefault().language) as? MutableMap<String, Any>?
                                    }
                                    val toObject =
                                        success?.documents!![0]?.toObject(AmbassadorProfiles::class.java)
                                    toObject?.checkForLangTranslation()
                                    toObject?.id = success?.documents.first().id
                                    _observableAmbassadorProgram.value = toObject
                                }
                        }
                    }
                }

            }
    }

    fun getAmbassadorApplication(ambProgID: String) {

        repository.db.collection("Ambassador_Settings")
            .whereEqualTo("type", "application")
            .whereEqualTo("ambassadorProgramId", ambProgID)
            .addSnapshotListener { success, err ->
                if (!success?.documents.isNullOrEmpty()) {
                    run {
                        if (Locale.getDefault().displayName != "en") {
                            repository.db.collection("Configuration")
                                .document("AmbassadorApplicationTranslations")
                                .addSnapshotListener { success_, err_ ->
                                    run {
                                        LangMapSingleton.langMap =
                                            success_?.data?.get(Locale.getDefault().language) as? MutableMap<String, Any>?
                                    }
                                    val toObject =
                                        success?.documents!![0]?.toObject(AmbassadorApplication::class.java)
                                    toObject?.checkForLangTranslation()
                                    _observableAmbassadorApplication.value = toObject
                                }
                        }
                    }
                }

            }
    }


}