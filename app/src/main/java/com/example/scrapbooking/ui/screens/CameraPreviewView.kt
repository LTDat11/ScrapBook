package com.example.scrapbooking.ui.screens

import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    onUseCaseCreated: (Preview) -> Unit // Truyền cái này ra ngoài để setup CameraX
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    ) {
        // Khởi tạo Preview UseCase
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        onUseCaseCreated(preview)
    }
}