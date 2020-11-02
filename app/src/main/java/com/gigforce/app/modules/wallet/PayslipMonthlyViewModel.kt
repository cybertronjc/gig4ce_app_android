package com.gigforce.app.modules.wallet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.BuildConfig
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.modules.wallet.models.PaySlipResponseModel
import com.gigforce.app.modules.wallet.models.Payslip
import com.gigforce.app.modules.wallet.remote.GeneratePaySlipService
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.network.RetrofitFactory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.*

class PayslipMonthlyViewModel constructor(
    private val walletRepository: WalletfirestoreRepository = WalletfirestoreRepository(),
    private var profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository(),
    private var paySlipService: GeneratePaySlipService = RetrofitFactory.generatePaySlipService()
) : ViewModel() {

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
                    payslip.uid,
                    payslip.getMonthNo(),
                    payslip.yearOfPayment
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
        uid: String,
        month: Int,
        year: Int
    ): PaySlipResponseModel {
        val generatePayslipUrl = if (BuildConfig.FLAVOR == "development")
            "https://d38v9ehujf.execute-api.ap-south-1.amazonaws.com/default/get-or-create-payment-advice-dev"
        else
            "https://d38v9ehujf.execute-api.ap-south-1.amazonaws.com/default/get-or-create-payment-advice-dev"

        val response = paySlipService.generatePayslip(
            generatePayslipUrl,
            uid,
            month,
            year
        )

        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Unable to generate payslip, ${response.message()}")
        }
    }

    private suspend fun downloadAndSavePaySlip(pdfDownloadLink: String, filesDir: File): File {
        val response = paySlipService.downloadPaySlip(pdfDownloadLink)

        if (response.isSuccessful) {
            val body = response.body()!!

            val paySlipFile = File(filesDir, "${System.currentTimeMillis()}.pdf")
            writeResponseBodyToDisk(body, paySlipFile)
            return paySlipFile
        } else {
            throw Exception("Unable to dowload payslip, ${response.message()}")
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