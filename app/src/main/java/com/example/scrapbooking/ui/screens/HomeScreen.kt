package com.example.scrapbooking.ui.screens

import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.example.scrapbooking.ui.components.CameraPreview
import com.example.scrapbooking.ui.components.StampPopup
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

