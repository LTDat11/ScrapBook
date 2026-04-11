package com.example.scrapbooking.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.scrapbooking.R
import com.example.scrapbooking.ui.components.ErrorView
import com.example.scrapbooking.ui.components.LoadingView
import com.example.scrapbooking.ui.state.AlbumUiState
import com.example.scrapbooking.viewmodel.AlbumViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    date: LocalDate,
    viewModel: AlbumViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stamps = when (uiState) {
        is AlbumUiState.Success -> (uiState as AlbumUiState.Success).photosByDate[date] ?: emptyList()
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_album_detail, date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState) {
                is AlbumUiState.Loading -> LoadingView()
                is AlbumUiState.Error -> ErrorView(message = (uiState as AlbumUiState.Error).message)
                is AlbumUiState.Success -> {
                    if (stamps.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = stringResource(R.string.no_photos), style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            items(stamps) { stamp ->
                                AsyncImage(
                                    model = stamp.path,
                                    contentDescription = stringResource(R.string.desc_stamp_photo),
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .aspectRatio(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
