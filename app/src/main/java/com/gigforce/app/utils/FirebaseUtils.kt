package com.gigforce.app.utils

import android.util.Log
import okhttp3.ResponseBody
import java.io.*

object FirebaseUtils {

    fun isFirebaseDownloadPath(path : String): Boolean {
        return path.contains("firebasestorage",true)
    }

    fun extractFilePath(downloadLink: String) : String{
        if(downloadLink.startsWith("http")){

            if(isFirebaseDownloadPath(downloadLink)){
                return downloadLink.substring(
                    downloadLink.lastIndexOf("%2F") + 3,
                    downloadLink.indexOf("?")
                )
            } else{
               return downloadLink.substring(
                    downloadLink.lastIndexOf('/') + 1,
                    downloadLink.length)
            }

        } else{
            throw IllegalArgumentException("Not a download link")
        }
    }
}