package com.yesyes.ddgmailgenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yesyes.ddgmailgenerator.data.DataStoreManager
import com.yesyes.ddgmailgenerator.data.DuckDuckGoRepository
import com.yesyes.ddgmailgenerator.ui.screen.DuckDuckGoMailGeneratorScreen
import com.yesyes.ddgmailgenerator.ui.theme.DDGMailGeneratorTheme
import com.yesyes.ddgmailgenerator.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val repository = DuckDuckGoRepository()
    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStoreManager = DataStoreManager(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            DDGMailGeneratorTheme {
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModel.Factory(repository, dataStoreManager)
                )
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DuckDuckGoMailGeneratorScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
