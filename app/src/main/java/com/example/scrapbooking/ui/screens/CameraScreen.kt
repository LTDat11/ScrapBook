package com.example.scrapbooking.ui.screens

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.scrapbooking.ui.components.StampOverlay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(onNavigateToGallery: () -> Unit) {
    // 1. Khai báo trạng thái quyền Camera
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    // 2. Kiểm tra trạng thái
    if (cameraPermissionState.status.isGranted) {
        // Đã có quyền -> Hiển thị giao diện Camera thực sự
        CameraContent(onNavigateToGallery)
    } else {
        // Chưa có quyền hoặc bị từ chối -> Hiển thị màn hình yêu cầu
        PermissionRequestScreen(
            shouldShowRationale = cameraPermissionState.status.shouldShowRationale,
            onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
        )
    }
}


@Composable
fun CameraContent(onNavigateToGallery: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Nơi giữ ImageCapture để gọi lệnh chụp ảnh
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Camera Preview
        CameraPreviewView { previewUseCase ->
            val cameraProvider = cameraProviderFuture.get()
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    previewUseCase,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Binding failed", e)
            }
        }

        // 2. Overlay Khung Tem (File mask.png của bạn)
        StampOverlay(modifier = Modifier.fillMaxSize())

        // 3. UI Nút bấm
        IconButton(
            onClick = onNavigateToGallery,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Collections, contentDescription = null, tint = Color.White)
        }

        FloatingActionButton(
            onClick = {
                // TODO: Gọi hàm capturePhoto(imageCapture) ở đây
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            containerColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = "Chụp")
        }
    }
}