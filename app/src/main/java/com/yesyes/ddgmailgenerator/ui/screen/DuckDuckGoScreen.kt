package com.yesyes.ddgmailgenerator.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yesyes.ddgmailgenerator.ui.theme.DDGMailGeneratorTheme
import com.yesyes.ddgmailgenerator.ui.viewmodel.MainUiState
import com.yesyes.ddgmailgenerator.ui.viewmodel.MainViewModel

@Composable
fun DuckDuckGoMailGeneratorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val token by viewModel.token.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is MainUiState.Success) {
            val email = (uiState as MainUiState.Success).email
            copyToClipboard(context, email)
            Toast.makeText(context, "Email generated and copied to clipboard", Toast.LENGTH_SHORT).show()
        } else if (uiState is MainUiState.Error) {
            Toast.makeText(context, "Failed to generate email", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DuckDuckGo Mail Generator",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = token,
            onValueChange = { viewModel.onTokenChange(it) },
            label = { Text("DUCKDUCKGO_ACCESS_TOKEN") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        val isLoading = uiState is MainUiState.Loading
        Button(
            onClick = { viewModel.generateEmail() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && token.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Generate Email")
            }
        }

        when (uiState) {
            is MainUiState.Success -> {
                val email = (uiState as MainUiState.Success).email
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Generated Email:",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = email,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            is MainUiState.Error -> {
                val error = (uiState as MainUiState.Error).message
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            else -> {}
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("DuckDuckGo Email", text)
    clipboard.setPrimaryClip(clip)
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    DDGMailGeneratorTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "DuckDuckGo Mail Generator",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("DUCKDUCKGO_ACCESS_TOKEN") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Email")
            }
        }
    }
}
