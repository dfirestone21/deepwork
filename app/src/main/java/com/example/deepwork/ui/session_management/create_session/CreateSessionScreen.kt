package com.example.deepwork.ui.session_management.create_session

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.compose.DeepWorkTheme
import com.example.compose.onPrimaryContainerDark
import com.example.deepwork.R
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.ui.components.TextField
import com.example.deepwork.ui.model.InputField
import com.example.deepwork.ui.model.TimeBlockUi
import com.example.deepwork.ui.util.UiEvent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun CreateSessionScreen(
    viewModel: CreateSessionViewModel = hiltViewModel(),
    onNavigate: (UiEvent.Navigate) -> Unit,
    onNavigateUp: () -> Unit
) {
    val uiState = viewModel.state

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)
                is UiEvent.NavigateUp -> onNavigateUp()
                is UiEvent.ShowSnackbar -> TODO()
            }
        }
    }

    CreateSessionContent(
        uiState = uiState,
        onNavigate = onNavigate,
        onEvent = viewModel::onEvent
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun CreateSessionContentPreview() {
    val timeBlocks = listOf(
        TimeBlock.deepWorkBlock(60.minutes),
        TimeBlock.shallowWorkBlock(30.minutes),
        TimeBlock.breakBlock(10.minutes)
    ).map { TimeBlockUi.fromDomain(it) }

    val uiState = CreateSessionUiState(
        name = InputField("Session name"),
        timeBlocks = timeBlocks
    )
    DeepWorkTheme {
        CreateSessionContent(
            uiState = uiState,
            onNavigate = {},
            onEvent = {}
        )
    }
}

@Composable
fun CreateSessionContent(
    uiState: CreateSessionUiState,
    onNavigate: (UiEvent.Navigate) -> Unit,
    onEvent: (CreateSessionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar =  {
            CreateSessionTopBar(
                onNavigateUp = { //TODO
                }
            )
        },
        floatingActionButton = { CreateSessionFab(onClick = {
            onEvent(CreateSessionEvent.FabClicked)
        }) }
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
                .padding(16.dp)
                .fillMaxSize()
                .padding(padding)
        ) {
            TextField(
                value = uiState.name.value,
                onValueChange = { onEvent(CreateSessionEvent.UpdateName(it)) },
                placeholder = "Session name",
                isError = uiState.name.message != null,
            )
            TimeBlocks(
                timeBlocks = uiState.timeBlocks
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionTopBar(
    onNavigateUp: () -> Unit
) {
    TopAppBar(
        title = { Text(text = "Create Session") },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        }
    )
}

@Composable
fun TimeBlocks(timeBlocks: List<TimeBlockUi>, modifier: Modifier = Modifier) {
    val totalDuration = timeBlocks.sumOf { it.duration.inWholeMinutes }.toDuration(DurationUnit.MINUTES)
    val baseWidth = 100.dp
    val minWidth = 60.dp
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        if (timeBlocks.isNotEmpty()) {
            Text(text = "Total: $totalDuration",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            LazyRow(
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .heightIn(min = 82.dp)
            ) {
                if (timeBlocks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .height(82.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No time blocks added",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                } else {
                    items(timeBlocks.size) { index ->
                        val timeBlock = timeBlocks[index]
                        val width = maxOf(
                            minWidth,
                            baseWidth * (timeBlock.duration.inWholeMinutes.toFloat() / 30f)
                        )
                        TimeBlockView(
                            timeBlock,
                            modifier = Modifier
                                .padding(2.dp)
                                .width(baseWidth * (timeBlock.duration.inWholeMinutes.toFloat() / 30f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeBlockView(timeBlock: TimeBlockUi, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = timeBlockColorFrom(timeBlock),
        shadowElevation = 6.dp,
        modifier = modifier
            .height(80.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            TimeBlockText(text = timeBlock.type.toString())
            TimeBlockText(text = timeBlock.duration.toString())
        }
    }
}

@Composable
fun TimeBlockText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}

@Composable
private fun timeBlockColorFrom(timeBlock: TimeBlockUi): Color {
    return when (timeBlock.type) {
        TimeBlockUi.Companion.Type.DEEP_WORK -> MaterialTheme.colorScheme.primary
        TimeBlockUi.Companion.Type.SHALLOW_WORK -> colorResource(id = R.color.timeblock_shallowwork)
        TimeBlockUi.Companion.Type.BREAK -> colorResource(id = R.color.timeblock_break)
    }
}

@Composable
fun CreateSessionFab(onClick: () -> Unit) {

    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Filled.Add, "Extended floating action button.") },
        text = { Text(text = "Add Block") },
        shape = RoundedCornerShape(12.dp),
        containerColor = MaterialTheme.colorScheme.primary,
    )
}
