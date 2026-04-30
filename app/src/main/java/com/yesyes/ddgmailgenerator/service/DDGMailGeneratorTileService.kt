package com.yesyes.ddgmailgenerator.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.yesyes.ddgmailgenerator.data.DataStoreManager
import com.yesyes.ddgmailgenerator.data.DuckDuckGoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DDGMailGeneratorTileService : TileService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var dataStoreManager: DataStoreManager
    private val repository = DuckDuckGoRepository()

    override fun onCreate() {
        super.onCreate()
        dataStoreManager = DataStoreManager(applicationContext)
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return

        serviceScope.launch {
            try {
                // Update tile to show loading state
                tile.state = Tile.STATE_ACTIVE
                tile.updateTile()

                val token = dataStoreManager.tokenFlow.first()
                if (token.isBlank()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DDGMailGeneratorTileService, "Please set token in app first", Toast.LENGTH_LONG).show()
                        tile.state = Tile.STATE_INACTIVE
                        tile.updateTile()
                    }
                    return@launch
                }

                val result = repository.generateEmail(token)
                result.onSuccess { email ->
                    copyToClipboard(email)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DDGMailGeneratorTileService, "Email copied: $email", Toast.LENGTH_SHORT).show()
                    }
                }.onFailure { error ->
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DDGMailGeneratorTileService, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                // Reset tile state
                tile.state = Tile.STATE_INACTIVE
                tile.updateTile()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DDGMailGeneratorTileService, "Unexpected error", Toast.LENGTH_SHORT).show()
                }
                tile.state = Tile.STATE_INACTIVE
                tile.updateTile()
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("DuckDuckGo Email", text)
        clipboard.setPrimaryClip(clip)
        
        // On Android 13+, the system already shows a confirmation for clipboard copy
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // No action needed, Toast handled in onClick
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
