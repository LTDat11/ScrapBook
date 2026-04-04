package com.example.scrapbooking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.scrapbooking.ui.components.StampOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(onNavigateToGallery: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("STAMP PRESS", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onNavigateToGallery) {
                        Icon(imageVector = Icons.Default.Collections, contentDescription = "Gallery")
                    }
                }
            )
        },
        floatingActionButton = {
            // Nút chụp ảnh thiết kế to, nổi bật ở giữa
            FloatingActionButton(
                onClick = { /* TODO: Trigger Camera Capture */ },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Capture", modifier = Modifier.size(25.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black) // Nền đen cho vùng camera
        ) {
            // 1. Chỗ này sau này sẽ là Camera Preview
            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
                Text("Camera Preview Placeholder", color = Color.White, modifier = Modifier.align(
                    Alignment.Center))
            }

            // 2. Đè khung Stamp Overlay lên trên
            StampOverlay()

            // 3. Hướng dẫn sử dụng nhỏ phía dưới
            Text(
                text = "Đưa vật thể vào giữa khung tem",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 100.dp)
            )
        }
    }
}