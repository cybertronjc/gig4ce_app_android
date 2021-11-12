package com.gigforce.core.documentFileHelper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.gigforce.core.logger.GigforceLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentTreeDelegate @Inject constructor(
    private val documentPrefHelper: DocumentPrefHelper,
    private val logger : GigforceLogger
) {
    
    companion object{

        const val FOLDER_GIGFORCE = "Gigforce"
        const val LOG_TAG = "DocumentTreeDelegate"
    }

    fun storageTreeSelected() = documentPrefHelper.documentUriSaved()



    fun handleDocumentTreeSelectionResult(
        context : Context,
        uri : Uri,
        onSuccess : (uri : Uri) -> Unit,
        onFailure : (e : Exception) -> Unit
    ){
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        checkIfGigforceFolderExistElseCreate(
            context,
            uri,
            onSuccess,
            onFailure
        )
    }

    private fun checkIfGigforceFolderExistElseCreate(
        context: Context,
        uri: Uri,
        onSuccess: (uri: Uri) -> Unit,
        onFailure: (e: Exception) -> Unit,
    ) {
        val rootTreeDocumentFile = DocumentFile.fromTreeUri(context, uri) ?: return

        if (!rootTreeDocumentFile.isDirectory) {

            logger.d(LOG_TAG,"user selected file instead of folder")
            Toast.makeText(
                context,
                "Please select a folder not a file",
                Toast.LENGTH_SHORT
            ).show()
            
            onFailure.invoke(Exception("Please select a folder not a file"))
            return
        }

        val finalUri: Uri
        if (rootTreeDocumentFile.name == FOLDER_GIGFORCE) {
            //user selected gigforce folder in SAF, nothing to do next
            logger.d(LOG_TAG,"user selected folder name gigforce")
            finalUri = uri
        } else {
            val gigforceFolderInCurrentSelectedFolder = rootTreeDocumentFile.findFile(
                FOLDER_GIGFORCE
            )
            val isGigforceFolderPresentInSelectedFolder = gigforceFolderInCurrentSelectedFolder != null

            if (isGigforceFolderPresentInSelectedFolder) {
                logger.d(LOG_TAG,"user selected folder, a sub-folder named Gigforce is already present in selected folder")

                finalUri = gigforceFolderInCurrentSelectedFolder!!.uri
            } else {
                //Create a folder name gigforce
                logger.d(LOG_TAG,"gigforce folder not present in selected folder, creating...")

                finalUri = rootTreeDocumentFile.createDirectory(FOLDER_GIGFORCE)!!.uri
            }
        }

        documentPrefHelper.saveReceivedDocumentTreeUri(finalUri)
        onSuccess.invoke(finalUri)
    }
}