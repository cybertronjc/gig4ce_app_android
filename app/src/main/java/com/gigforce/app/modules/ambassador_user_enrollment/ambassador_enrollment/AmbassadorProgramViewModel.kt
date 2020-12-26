package com.gigforce.app.modules.ambassador_user_enrollment.ambassador_enrollment

import androidx.lifecycle.ViewModel
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

    fun getAmbassadorProfiles() {
        repository.getCollectionReference().limit(1).addSnapshotListener { success, err ->
            run {
                if (Locale.getDefault().displayName != "en") {
                    repository.getCollectionReference()
                        .document(success?.documents?.get(0)?.id ?: "").collection("metadata")
                        .document("languages").addSnapshotListener { success_, err_ ->
                            run {
                                LangMapSingleton.langMap =
                                    success_?.data?.get(Locale.getDefault().language) as? MutableMap<String, String>?
                            }
                            val toObject =
                                success?.documents!![0]?.toObject(AmbassadorProfiles::class.java)
                            toObject?.checkForLangTranslation()
                            _observableAmbassadorProgram.value = toObject
                        }
                }
            }
        }
    }

}