package com.example.scrapbooking.ui.screens

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.scrapbooking.R
import com.example.scrapbooking.ui.components.ErrorView
import com.example.scrapbooking.ui.components.LoadingView
import com.example.scrapbooking.ui.components.PermissionDeniedView
import com.example.scrapbooking.ui.state.HomeUiState
import com.example.scrapbooking.viewmodel.HomeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen (viewModel: HomeViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    // Yêu cầu quyền khi lần đầu vào màn hình
    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    // Cập nhật UI state dựa trên quyền
    LaunchedEffect(cameraPermissionState.status) {
        when (cameraPermissionState.status) {
            PermissionStatus.Granted -> viewModel.setCameraReady()
            is PermissionStatus.Denied -> {
                if ((cameraPermissionState.status as PermissionStatus.Denied).shouldShowRationale) {
                    viewModel.setError("Cần cấp quyền camera để sử dụng")
                } else {
                    viewModel.setPermissionDenied()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Hiển thị camera nếu đã có quyền
        if (cameraPermissionState.status == PermissionStatus.Granted) {
            CameraPreviewWithOverlay(
                modifier = Modifier.fillMaxSize(),
                lifecycleOwner = lifecycleOwner,
                onError = { error ->
                    viewModel.setError("Camera error: $error")
                }
            )
        } else {
            // Hiển thị thông báo khi chưa có quyền
            when (val state = viewModel.uiState.collectAsState().value) {
                HomeUiState.PermissionDenied -> PermissionDeniedView(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                )
                is HomeUiState.Error -> ErrorView(message = state.message)
                else -> LoadingView()
            }
        }

        // Overlay khung hình PNG ở giữa màn hình
        Image(
            painter = painterResource(id = R.drawable.mask2), // file frame.png trong res/drawable
            contentDescription = "Camera Frame",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center
        )
    }
}

@Composable
private fun CameraPreviewWithOverlay(
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var previewUseCase by remember { mutableStateOf<Preview?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Khởi tạo CameraProvider
    LaunchedEffect(Unit) {
        try {
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener({
                cameraProvider = providerFuture.get()
            }, ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            onError(e.message ?: "Camera init failed")
        }
    }

    // Theo dõi lifecycle để bind/unbind camera
    DisposableEffect(lifecycleOwner, cameraProvider) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    bindCameraUseCases(
                        context,
                        cameraProvider,
                        previewUseCase,
                        cameraExecutor,
                        onError
                    )
                }

                Lifecycle.Event.ON_PAUSE -> {
                    unbindCameraUseCases(cameraProvider)
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            cameraExecutor.shutdown()
            unbindCameraUseCases(cameraProvider)
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                this.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                // PreviewUseCase sẽ được bind sau, nhưng cần giữ tham chiếu để bind
                previewUseCase = Preview.Builder().build().also { preview ->
                    preview.setSurfaceProvider(this.surfaceProvider)
                }
            }
        },
        modifier = modifier
    )
}

private fun bindCameraUseCases(
    context: android.content.Context,
    cameraProvider: ProcessCameraProvider?,
    previewUseCase: Preview?,
    cameraExecutor: ExecutorService,
    onError: (String) -> Unit
) {
    if (cameraProvider == null || previewUseCase == null) return
    try {
        cameraProvider.unbindAll()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        cameraProvider.bindToLifecycle(
            context as androidx.lifecycle.LifecycleOwner,
            cameraSelector,
            previewUseCase
        )
    } catch (e: Exception) {
        onError(e.message ?: "Camera binding failed")
    }
}

private fun unbindCameraUseCases(cameraProvider: ProcessCameraProvider?) {
    cameraProvider?.unbindAll()
}
