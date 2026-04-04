package com.example.scrapbooking.ui.screens

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    onImageCaptureCreated: (ImageCapture) -> Unit // Trả về ImageCapture để nút chụp sử dụng
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // LaunchedEffect sẽ chạy khi màn hình này được nạp
    LaunchedEffect(Unit) {
        val cameraProvider = cameraProviderFuture.get()

        // Cấu hình Preview
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        // Cấu hình ImageCapture (để chụp ảnh)
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(previewView.display.rotation)
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // Quan trọng: Phải unbind trước khi bind mới
            cameraProvider.unbindAll()

            // Kết nối Camera với Lifecycle của Activity/Fragment
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            // Trả imageCapture về cho CameraContent quản lý
            onImageCaptureCreated(imageCapture)

        } catch (exc: Exception) {
            Log.e("CameraX", "Use case binding failed", exc)
        }
    }

    // Hiển thị View truyền thống trong Compose
    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}