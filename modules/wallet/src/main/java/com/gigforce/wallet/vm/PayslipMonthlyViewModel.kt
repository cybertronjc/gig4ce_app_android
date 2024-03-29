package com.gigforce.wallet.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.retrofit.PaySlipResponseModel
import com.gigforce.wallet.models.Payslip
import com.gigforce.core.retrofit.GeneratePaySlipService
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.di.repo.IProfileFirestoreRepository
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.core.utils.Lce
import com.gigforce.wallet.WalletfirestoreRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import dagger.Provides
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.*
import javax.inject.Inject

@HiltViewModel
class PayslipMonthlyViewModel @Inject constructor(
    private val walletRepository: WalletfirestoreRepository = WalletfirestoreRepository(),
    private val profileFirebaseRepository : IProfileFirestoreRepository,
    private val buildConfig:IBuildConfigVM,
    private var paySlipService: GeneratePaySlipService
) : ViewModel() {
//    @Inject lateinit var profileFirebaseRepository : IProfileFirestoreRepository
//    @Inject lateinit var buildConfig:IBuildConfig
    init {
        getProfileData()
    }

    private var profileListenerRegistration: ListenerRegistration? = null

    private val _userProfileData: MutableLiveData<ProfileData> = MutableLiveData()
    var userProfileData: LiveData<ProfileData> = _userProfileData

    fun getProfileData() {
        profileListenerRegistration = profileFirebaseRepository.getDBCollection()
            .addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
                if (e != null) {
                    Log.w("ProfileViewModel", "Listen failed", e)
                    return@EventListener
                }

                if (value!!.data == null) {
                    profileFirebaseRepository.createEmptyProfile()
                } else {
                    Log.d("ProfileViewModel", value.data.toString())
                    _userProfileData.postValue(
                        value.toObject(ProfileData::class.java)
                    )
                    Log.d("ProfileViewModel", userProfileData.toString())
                }
            })
    }


    private val _monthlySlips = MutableLiveData<Lce<List<Payslip>>>()
    val monthlySlips: LiveData<Lce<List<Payslip>>> = _monthlySlips

    fun getPaySlips() = viewModelScope.launch {
        _monthlySlips.value = Lce.loading()

        try {
            val paySlips = walletRepository.getPaySlips()
            _monthlySlips.value = Lce.content(paySlips)
        } catch (e: Exception) {
            _monthlySlips.value = Lce.error(e.message!!)
        }
    }

    private val _downloadPaySlip = MutableLiveData<Lce<File>>()
    val downloadPaySlip: LiveData<Lce<File>> = _downloadPaySlip

    fun downloadPaySlip(
        payslip: Payslip,
        filesDir: File
    ) = viewModelScope.launch {
        _downloadPaySlip.value = Lce.loading()

        try {

            val paySlipUrl = if (payslip.pdfDownloadLink != null) {
                //Payslip already generated ,Just download it
                payslip.pdfDownloadLink!!

            } else {
                //Generate and download
                val response = generatePaySlip(
                    payslip.id
                )

                response.downloadLink!!
            }

            val file = downloadAndSavePaySlip(paySlipUrl, filesDir)
            _downloadPaySlip.value = Lce.content(file)
        } catch (e: Exception) {
            _downloadPaySlip.value = Lce.error(e.message!!)
        }
    }


    private suspend fun generatePaySlip(
        payslipId: String
    ): PaySlipResponseModel {

        val response = paySlipService.generatePayslip(
          buildConfig.getGeneratePayslipURL(),
            payslipId
        )

        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Unable to generate payslip, ${response.message()}")
        }
    }

    private suspend fun downloadAndSavePaySlip(pdfDownloadLink: String, filesDir: File): File {
        val fileName: String = pdfDownloadLink.substring(
            pdfDownloadLink.lastIndexOf('/') + 1,
            pdfDownloadLink.length
        )

        val paySlipFile = File(filesDir, fileName)
        if (paySlipFile.exists()) {
            Log.d("PayslipMonthlyViewModel", "File Present in local")
            //File Present in Local
            return paySlipFile
        } else {
            val response = paySlipService.downloadPaySlip(pdfDownloadLink)

            if (response.isSuccessful) {
                val body = response.body()!!
                writeResponseBodyToDisk(body, paySlipFile)
                return paySlipFile
            } else {
                throw Exception("Unable to dowload payslip, ${response.message()}")
            }
        }
    }

    private fun writeResponseBodyToDisk(body: ResponseBody, destFile: File): Boolean {
        return try {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize: Long = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(destFile)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    Log.d("DownloadPath", "file download: $fileSizeDownloaded of $fileSize")
                }
                outputStream.flush()
                true
            } catch (e: IOException) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            false
        }
    }


    override fun onCleared() {
        super.onCleared()
        profileListenerRegistration?.remove()
    }
}