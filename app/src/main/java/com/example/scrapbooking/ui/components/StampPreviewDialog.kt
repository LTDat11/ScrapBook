package com.example.scrapbooking.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun StampPreviewDialog(
    bitmap: Bitmap?,
    onDismiss: () -> Unit,
    onSave: (Bitmap) -> Unit
) {
    if (bitmap == null) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium, // Bo góc chuẩn Material 3
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lá tem của bạn",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(), // Chỉ hiển thị con tem hoàn chỉnh
                        contentDescription = "Stamp Preview",
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(1f) // Hoặc tỷ lệ mask của bạn
                            .padding(bottom = 16.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Nút Hủy
                    TextButton(onClick = onDismiss) {
                        Text("Chụp lại", color = MaterialTheme.colorScheme.error)
                    }
                    // Nút Lưu
                    Button(onClick = { onSave(bitmap) }) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lưu Tem")
                    }
                }
            }
        }
    }
}