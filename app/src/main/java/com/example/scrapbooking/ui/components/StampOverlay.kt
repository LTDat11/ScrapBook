package com.example.scrapbooking.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.scrapbooking.R

@Composable
fun StampOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Lớp Mask để người dùng nhìn thấy khung cắt
        Image(
            painter = painterResource(id = R.drawable.mask), // File mask bạn đã gửi
            contentDescription = "Stamp Mask",
            modifier = Modifier
                .fillMaxSize(0.8f) // Chiếm 80% màn hình để chừa chỗ cho UI khác
                .aspectRatio(1f), // tỉ lệ theo file
            contentScale = ContentScale.Fit,
        )
    }
}