package com.example.deepwork.ui.session_management.create_session.add_time_block

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.compose.DeepWorkTheme
import com.example.deepwork.R
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.ui.components.ActionButton
import com.example.deepwork.ui.components.Header
import com.example.deepwork.ui.components.SecondaryButton
import com.example.deepwork.ui.components.TextField
import com.example.deepwork.ui.model.InputField
import com.example.deepwork.ui.util.UiEvent

@Composable
fun AddTimeBlockScreen(
    viewModel: AddTimeBlockViewModel = hiltViewModel(),
    onNavigate: (UiEvent.Navigate) -> Unit,
    onNavigateUp: () -> Unit
) {
    LaunchedEffect(true) {
        viewModel.uiEvent.collect {
            when (it) {
                is UiEvent.Navigate -> onNavigate(it)
                is UiEvent.NavigateUp -> onNavigateUp()
                is UiEvent.ShowSnackbar -> TODO()
            }
        }
    }
    AddTimeBlockContent(
        state = viewModel.state,
        onNavigate = onNavigate,
        onNavigateUp = onNavigateUp,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun AddTimeBlockContent(
    state: AddTimeBlockState,
    onNavigate: (UiEvent.Navigate) -> Unit,
    onNavigateUp: () -> Unit,
    onEvent: (AddTimeBlockEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Header("Add Block")
        TimeBlockSelectionChips(
            state = state,
            onEvent = onEvent
        )
        DurationTextField(
            duration = state.duration,
            onDurationChanged = { onEvent(AddTimeBlockEvent.DurationChanged(it)) }
        )
        Spacer(modifier = Modifier.weight(1f))
        AddTimeBlockButtons(
            state = state,
            onEvent = onEvent,
        )
    }
}

@Composable
fun TimeBlockChip(
    title: String,
    isSelected: Boolean = false,
    color: Color,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = { Text(title) },
        selected = isSelected,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = color,
            selectedContainerColor = color,
            labelColor = Color.White,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = color,
            enabled = true,
            selected = isSelected
        ),
        shape = RoundedCornerShape(24.dp),
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Done icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                    tint = Color.White
                )
            }
        } else {
            null
        }
    )
}

@Composable
fun TimeBlockSelectionChips(
    state: AddTimeBlockState,
    onEvent: (AddTimeBlockEvent) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeBlockChip(
            title = "Deep Work",
            isSelected = state.selectedBlockType == TimeBlock.BlockType.DEEP,
            color = MaterialTheme.colorScheme.primary,
            onClick = { onEvent(AddTimeBlockEvent.BlockTypeSelected(TimeBlock.BlockType.DEEP)) }
        )
        TimeBlockChip(
            title = "Shallow Work",
            isSelected = state.selectedBlockType == TimeBlock.BlockType.SHALLOW,
            color = colorResource(R.color.timeblock_shallowwork),
            onClick = { onEvent(AddTimeBlockEvent.BlockTypeSelected(TimeBlock.BlockType.SHALLOW)) }
        )
        TimeBlockChip(
            title = "Break",
            isSelected = state.selectedBlockType == TimeBlock.BlockType.BREAK,
            color = colorResource(R.color.timeblock_break),
            onClick = { onEvent(AddTimeBlockEvent.BlockTypeSelected(TimeBlock.BlockType.BREAK)) }
        )
    }
}

@Composable
fun DurationTextField(
    duration: InputField,
    onDurationChanged: (String) -> Unit
) {
    TextField(
        value = duration.value.ifBlank { "" },
        onValueChange = onDurationChanged,
        placeholder = "Duration (minutes)",
        isError = duration.isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        supportingText = duration.message
    )
}

@Composable
fun AddTimeBlockButtons(
    state: AddTimeBlockState,
    onEvent: (AddTimeBlockEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SecondaryButton(
            value = "Cancel",
            onClick = { onEvent(AddTimeBlockEvent.CancelClicked) }
        )
        ActionButton(
            value = "Save",
            isEnabled = state.isValid,
            onClick = { onEvent(AddTimeBlockEvent.SaveClicked) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddTimeBlockContentPreview() {
    DeepWorkTheme {
        AddTimeBlockContent(
            state = AddTimeBlockState(
                selectedBlockType = TimeBlock.BlockType.DEEP,
                duration = InputField(),
                isValid = true
            ),
            onNavigate = {},
            onNavigateUp = {},
            onEvent = {}
        )
    }
}