package com.osemu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appViewModel: AppViewModel = viewModel()
            val uiState by appViewModel.uiState.collectAsState()

            OsEmuTheme(appTheme = uiState.currentTheme) {
                AppNavigation(
                    appViewModel = appViewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                )
            }
        }
    }
}
