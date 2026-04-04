package com.example.scrapbooking.ui.screens

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.scrapbooking.ui.components.StampOverlay
import com.example.scrapbooking.ui.components.StampPreviewDialog
import com.example.scrapbooking.util.BitmapUtils
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

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Gọi Preview và lấy instance của imageCapture
        CameraPreviewView(
            onImageCaptureCreated = { capture ->
                imageCapture = capture
            }
        )

        // Overlay khung tem của bạn
        StampOverlay(modifier = Modifier.fillMaxSize())

        // Nút chụp ảnh
        FloatingActionButton(
            onClick = {
                imageCapture?.let { it -> // Có ImageCapture mới chụp
                    it.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                // GỌI HÀM "ÉP TEM" DUY NHẤT VÀ FIX XOAY
                                // (Hàm mới này nhận thẳng ImageProxy)
                                val stampBitmap = BitmapUtils.createStampBitmapFromImageProxy(context, image)

                                // Cập nhật State để hiện Pop-up
                                capturedBitmap = stampBitmap
                                showPreviewDialog = true

                                // KHÔNG CẦN image.close() ở đây nữa, vì đã close trong BitmapUtils rồi
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraX", "Capture failed: ${exception.message}", exception)
                                Toast.makeText(context, "Lỗi chụp ảnh!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            containerColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = "Chụp")
        }

        if (showPreviewDialog) {
            StampPreviewDialog(
                bitmap = capturedBitmap,
                onDismiss = {
                    showPreviewDialog = false
                    capturedBitmap = null // Chụp lại thì xóa bitmap cũ
                },
                onSave = { bitmap ->
                    // TODO: Gọi logic Lưu file vật lý vào máy ở bước sau
                    showPreviewDialog = false
                    capturedBitmap = null
                    Toast.makeText(context, "Đã lưu tem thành công!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Nút Gallery
        IconButton(
            onClick = onNavigateToGallery,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Collections, contentDescription = null, tint = Color.White)
        }
    }
}