package com.example.scrapbooking.ui.components

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.scrapbooking.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

// ─────────────────────────────────────────────────────────────────────────────
// Stamp Popup
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StampPopup(
    bitmap: Bitmap,
    date: String,
    time: String,
    location: String?,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
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

        // Bottom sheet showing metadata + actions (white background)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = dimensionResource(id = com.example.scrapbooking.R.dimen.sheet_bottom_padding))
                .alpha(contentAlpha),
            color = Color.White,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(
                topStart = dimensionResource(id = com.example.scrapbooking.R.dimen.sheet_corner),
                topEnd = dimensionResource(id = com.example.scrapbooking.R.dimen.sheet_corner)
            )
        ) {
            Column(modifier = Modifier.padding(dimensionResource(id = com.example.scrapbooking.R.dimen.sheet_padding))) {
                Text(text = stringResource(id = R.string.sheet_title), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(dimensionResource(id = com.example.scrapbooking.R.dimen.sheet_spacing)))
                Text(text = stringResource(id = R.string.image_info_date, date))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = stringResource(id = R.string.image_info_time, time))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = stringResource(id = R.string.image_info_location, location ?: "-"))
                Spacer(modifier = Modifier.height(dimensionResource(id = com.example.scrapbooking.R.dimen.sheet_spacing)))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    androidx.compose.material3.TextButton(onClick = { dismissWithAnimation(onDismiss) }) {
                        Text(text = stringResource(id = R.string.action_retry))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { dismissWithAnimation(onSave) }) {
                        Text(text = stringResource(id = R.string.action_save_photo))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Camera Preview (tách logic camera, expose PreviewView reference)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CameraPreview(
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
