package com.gigforce.giger_app.repo

import com.gigforce.common_ui.viewdatamodels.VideoItemCardDVM
import com.gigforce.core.extensions.getOrThrow
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface IHelpVideosDataRepository {
    suspend fun requestData(limit : Long): List<Any>
}

class HelpVideosDataRepository @Inject constructor() : IHelpVideosDataRepository {
    private val collectionName = "Help_Videos"
    override suspend fun requestData(limit : Long): List<Any> {
        val _data = ArrayList<VideoItemCardDVM>()
        val allDataDocs = FirebaseFirestore.getInstance()
            .collection(collectionName)
            .orderBy("index").limit(limit).getOrThrow()
        val allDocuments = allDataDocs.documents
        for (item in allDocuments) {
            val obj = item.toObject(VideoItemCardDVM::class.java)
            obj?.let {
                _data.add(obj)
            }
        }
        return _data

    }


}