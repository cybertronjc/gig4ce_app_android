package com.gigforce.core.fb

object FirebaseUtils {

    fun isFirebaseDownloadPath(path: String): Boolean {
        return path.contains("firebasestorage", true)
    }

    fun extractFilePath(downloadLink: String): String {
        if (downloadLink.startsWith("http")) {

            if (isFirebaseDownloadPath(downloadLink)) {
                return downloadLink.substring(
                    downloadLink.lastIndexOf("%2F") + 3,
                    downloadLink.indexOf("?")
                )
            } else {
                return downloadLink.substring(
                    downloadLink.lastIndexOf('/') + 1,
                    downloadLink.length
                )
            }

        } else {

            if (downloadLink.contains("/")) {
                return downloadLink.substring(
                    downloadLink.lastIndexOf('/') + 1,
                    downloadLink.length
                )
            } else {
                return downloadLink
            }



            throw IllegalArgumentException("Not a download link")
        }
    }
}