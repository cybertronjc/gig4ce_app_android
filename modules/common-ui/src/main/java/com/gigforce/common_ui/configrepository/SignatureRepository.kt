package com.gigforce.common_ui.configrepository

import android.net.Uri
import android.os.FileUtils
import androidx.core.net.toFile
import com.gigforce.common_ui.ext.bodyOrThrow
import com.gigforce.common_ui.remote.SignatureImageService
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignatureRepository @Inject constructor(
    private val signatureImageService: SignatureImageService
)  {

    suspend fun uploadSignatureImageAndGetProcessImageUrl(
        uri : Uri
    ) : String{
        val file = uri.toFile()
        val imageBody = RequestBody.create(MediaType.parse("image/*"), file)
        val formData = MultipartBody.Part.createFormData("file", file.name, imageBody)

        signatureImageService.uploadSignatureImage(
            formData
        ).bodyOrThrow()
    }
}