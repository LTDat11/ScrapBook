package com.example.scrapbooking.ui.screens

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.example.scrapbooking.ui.components.StampShape
import com.example.scrapbooking.ui.state.HomeUiState
import com.example.scrapbooking.viewmodel.HomeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
// ─────────────────────────────────────────────────────────────────────────────
// HomeScreen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Giữ tham chiếu đến PreviewView để chụp bitmap
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // Yêu cầu quyền lần đầu
    LaunchedEffect(Unit) { cameraPermissionState.launchPermissionRequest() }

    // Đồng bộ trạng thái quyền → ViewModel
    LaunchedEffect(cameraPermissionState.status) {
        when (cameraPermissionState.status) {
            PermissionStatus.Granted -> viewModel.setCameraReady()
            is PermissionStatus.Denied -> {
                val denied = cameraPermissionState.status as PermissionStatus.Denied
                if (denied.shouldShowRationale) viewModel.setError(context.getString(R.string.error_camera_rationale))
                else viewModel.setPermissionDenied()
            }
        }
    }

    // Animation thu nhỏ khi nhấn mask2
    var isMaskPressed by remember { mutableStateOf(false) }
    val maskScale by animateFloatAsState(
        targetValue = if (isMaskPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f),
        label = "maskPressScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Camera preview ──────────────────────────────────────────────────
        if (cameraPermissionState.status == PermissionStatus.Granted) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                lifecycleOwner = lifecycleOwner,
                onPreviewViewReady = { previewViewRef = it },
                onError = { viewModel.setError(context.getString(R.string.error_camera_format, it)) }
            )
        } else {
            when (uiState) {
                is HomeUiState.PermissionDenied ->
                    PermissionDeniedView(onRequestPermission = { cameraPermissionState.launchPermissionRequest() })
                is HomeUiState.Error ->
                    ErrorView(message = (uiState as HomeUiState.Error).message)
                else -> LoadingView()
            }
        }

        // ── Khung mask2 — có hiệu ứng nhấn + trigger chụp ảnh ─────────────
        Image(
            painter = painterResource(id = R.drawable.mask2),
            contentDescription = stringResource(R.string.desc_stamp_frame),
            modifier = Modifier
                .fillMaxSize()
                .scale(maskScale)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isMaskPressed = true
                            tryAwaitRelease()
                            isMaskPressed = false
                        },
                        onTap = {
                            previewViewRef?.let { pv -> viewModel.captureStamp(pv) }
                        }
                    )
                },
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center
        )

        // ── Stamp popup ─────────────────────────────────────────────────────
        if (uiState is HomeUiState.CapturedStamp) {
            StampPopup(
                bitmap = (uiState as HomeUiState.CapturedStamp).bitmap,
                onDismiss = { viewModel.dismissStamp() },
                onSave = { viewModel.saveStamp() }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stamp Popup
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StampPopup(bitmap: Bitmap, onDismiss: () -> Unit, onSave: () -> Unit) {
    var animateIn by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        animateIn = true
    }

    fun dismissWithAnimation(action: () -> Unit) {
        animateIn = false
        coroutineScope.launch {
            delay(300)
            action()
        }
    }

    // Khoảng cách bay lên
    val translateY by animateFloatAsState(
        targetValue = if (animateIn) -100f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
        label = "translateY"
    )

    // Phóng to lên từ kích thước bằng lỗ hổng mask
    val scale by animateFloatAsState(
        targetValue = if (animateIn) 1.5f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
        label = "scale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(300),
        label = "contentAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f * contentAlpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // Ngăn click ra ngoài
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(y = translateY.dp)
                .scale(scale)
                .size(width = 155.dp, height = 190.dp)
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.desc_stamp_photo),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(StampShape(hPerforations = 7, vPerforations = 9)),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (translateY + 190).dp)
                .fillMaxWidth(0.7f)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { dismissWithAnimation(onDismiss) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text(stringResource(R.string.action_cancel), color = Color.White)
                }
                
                Button(
                    onClick = { dismissWithAnimation(onSave) }
                ) {
                    Text(stringResource(R.string.action_save_photo))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Camera Preview (tách logic camera, expose PreviewView reference)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner,
    onPreviewViewReady: (PreviewView) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var previewUseCase by remember { mutableStateOf<Preview?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(Unit) {
        try {
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({ cameraProvider = future.get() }, ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            onError(e.message ?: context.getString(R.string.error_camera_init))
        }
    }

    DisposableEffect(lifecycleOwner, cameraProvider) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME ->
                    bindCamera(context, cameraProvider, previewUseCase, onError)
                Lifecycle.Event.ON_PAUSE ->
                    cameraProvider?.unbindAll()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            cameraExecutor.shutdown()
            cameraProvider?.unbindAll()
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                previewUseCase = Preview.Builder().build().also {
                    it.setSurfaceProvider(surfaceProvider)
                }
                onPreviewViewReady(this)     // trả reference ra ngoài
            }
        },
        modifier = modifier
    )
}

private fun bindCamera(
    context: android.content.Context,
    cameraProvider: ProcessCameraProvider?,
    previewUseCase: Preview?,
    onError: (String) -> Unit
) {
    if (cameraProvider == null || previewUseCase == null) return
    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            context as androidx.lifecycle.LifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            previewUseCase
        )
    } catch (e: Exception) {
        onError(e.message ?: context.getString(R.string.error_camera_bind))
    }
}
