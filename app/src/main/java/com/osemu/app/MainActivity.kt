package com.osemu.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.osemu.app.ui.navigation.AppNavigation
import com.osemu.app.ui.navigation.AppViewModel
import com.osemu.app.ui.theme.OsEmuTheme

class MainActivity : ComponentActivity() {

    private lateinit var appViewModel: AppViewModel

    // SAF folder picker for ROM directories
    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            // Persist permission so we can access this folder across app restarts
            contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            appViewModel.scanUri(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            appViewModel = viewModel()
            val uiState by appViewModel.uiState.collectAsState()

            OsEmuTheme(appTheme = uiState.currentTheme) {
                AppNavigation(
                    appViewModel = appViewModel,
                    onPickFolder = { folderPickerLauncher.launch(null) },
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                )
            }
        }
    }
}
