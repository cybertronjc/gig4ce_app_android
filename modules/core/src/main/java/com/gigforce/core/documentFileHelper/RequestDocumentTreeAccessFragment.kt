package com.gigforce.core.documentFileHelper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gigforce.core.R
import com.gigforce.core.logger.GigforceLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RequestDocumentTreeAccessFragment : Fragment() {

    companion object {
        const val LOG_TAG = "RequestDocumentTreeAccessFragment"
        const val FOLDER_GIGFORCE = "Gigforce"
        const val REQUEST_CODE_STORAGE_ACCESS = 2333
        const val REQUEST_DOCUMENT_TREE_ACCESS_FRAGMENT = "RequestDocumentTreeAccessFragment"
    }

    @Inject
    lateinit var documentPrefHelper: DocumentPrefHelper

    @Inject
    lateinit var logger: GigforceLogger

    private val openDocumentTreeContract = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) {
        if (it == null) return@registerForActivityResult

        requireContext().contentResolver.takePersistableUriPermission(
            it,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        checkIfGigforceFolderExistElseCreate(it)
    }

    private fun checkIfGigforceFolderExistElseCreate(
        uri: Uri
    ) {
        val rootTreeDocumentFile = DocumentFile.fromTreeUri(requireContext(), uri) ?: return

        if (!rootTreeDocumentFile.isDirectory) {

            logger.d(LOG_TAG,"user selected file instead of folder")
            Toast.makeText(
                requireContext(),
                "Please select a folder not a file",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val finalUri: Uri
        if (rootTreeDocumentFile.name == FOLDER_GIGFORCE) {
            //user selected gigforce folder in SAF, nothing to do next
            logger.d(LOG_TAG,"user selected folder name gigforce")
            finalUri = uri
        } else {
            val gigforceFolderInCurrentSelectedFolder = rootTreeDocumentFile.findFile(FOLDER_GIGFORCE)
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
        findNavController().navigateUp()
    }


    private lateinit var grantPermission: Button
    private var askedOneTime = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(
        R.layout.fragment_request_document_tree_access,
        container,
        false
    )


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)

        if(!askedOneTime){
            openDocumentTreeContract.launch(
                null
            )
            askedOneTime = true
        }
    }

    private fun findViews(view: View) {
        grantPermission = view.findViewById(R.id.grant_access_btn)
        grantPermission.setOnClickListener {
            openDirectory()
        }
    }

    private fun openDirectory() {
        openDocumentTreeContract.launch(
            null
        )
    }
}