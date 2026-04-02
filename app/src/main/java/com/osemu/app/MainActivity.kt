package com.osemu.app

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
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

    // Runtime permission request for storage (Android <= 12)
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            appViewModel.scanDefaultPaths()
        } else {
            Toast.makeText(this, "Storage permission needed. Use 'Add ROMs' instead.", Toast.LENGTH_LONG).show()
        }
    }

    // MANAGE_EXTERNAL_STORAGE for Android 11+
    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            appViewModel.scanDefaultPaths()
        } else {
            Toast.makeText(this, "Storage access needed. Use 'Add ROMs' instead.", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestScanWithPermission() {
        when {
            // Android 11+ needs MANAGE_EXTERNAL_STORAGE
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (Environment.isExternalStorageManager()) {
                    appViewModel.scanDefaultPaths()
                } else {
                    try {
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                        )
                        manageStorageLauncher.launch(intent)
                    } catch (e: Exception) {
                        // Fallback: just try scanning (will catch SecurityExceptions)
                        appViewModel.scanDefaultPaths()
                    }
                }
            }
            // Android 6-10 needs READ_EXTERNAL_STORAGE
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                    appViewModel.scanDefaultPaths()
                } else {
                    storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            // Older Android - just scan
            else -> appViewModel.scanDefaultPaths()
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
                    onScanWithPermission = { requestScanWithPermission() },
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                )
            }
        }
    }
}
