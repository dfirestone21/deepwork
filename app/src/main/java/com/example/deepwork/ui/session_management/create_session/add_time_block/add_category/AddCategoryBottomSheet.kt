package com.example.deepwork.ui.session_management.create_session.add_time_block.add_category

import androidx.annotation.ColorInt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.compose.DeepWorkTheme
import com.example.deepwork.ui.components.ActionButton
import com.example.deepwork.ui.components.Header
import com.example.deepwork.ui.components.SecondaryButton
import com.example.deepwork.ui.components.TextField
import com.example.deepwork.ui.model.InputField
import com.example.deepwork.ui.util.ObserveAsEvents
import com.example.deepwork.ui.util.UiEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryBottomSheet(
    viewModel: AddCategoryViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit
) {
    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is UiEvent.Navigate -> TODO()
            is UiEvent.NavigateUp -> onDismiss()
            is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
        }
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        AddCategoryContent(
            state = viewModel.state,
            onEvent = viewModel::onEvent,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun AddCategoryContent(
    state: AddCategoryState,
    onEvent: (AddCategoryEvent) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Header(text = "Create Category")
        TextField(
            value = state.name.value,
            label = "Name",
            placeholder = "Writing",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            ),
            onValueChange = { onEvent(AddCategoryEvent.NameUpdated(it)) }
        )
        ColorSelector(
            colors = state.availableColors,
            selectedColor = state.selectedColor,
            onSelected = { onEvent(AddCategoryEvent.ColorSelected(it)) }
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Buttons(
            saveEnabled = state.isValid,
            onSave = { onEvent(AddCategoryEvent.SaveClicked) },
            onCancel = onDismiss
        )
    }
}

@Composable
fun ColorSelector(
    @ColorInt colors: List<Int>,
    @ColorInt selectedColor: Int? = null,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Text("Select a Color")
    LazyVerticalGrid(
        columns = GridCells.Adaptive(80.dp)) {
        for (color in colors) {
            item {
                SelectableColor(
                    color = color,
                    isSelected = color == selectedColor,
                    onSelected = { selectedColor -> onSelected(selectedColor) },
                    modifier = modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun SelectableColor(
    @ColorInt color: Int,
    onSelected: (Int) -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .size(width = 80.dp, height = 64.dp)
            .clickable { onSelected(color) },
        shadowElevation = 6.dp,
        border = if (isSelected) BorderStroke(
            3.dp,
            MaterialTheme.colorScheme.onPrimaryContainer
        ) else null
    ) {
        Box(
            modifier = Modifier.background(Color(color))
        )
    }
}

@Composable
private fun Buttons(
    saveEnabled: Boolean = false,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        SecondaryButton(value = "Cancel", onClick = onCancel)
        ActionButton(value = "Save", isEnabled = saveEnabled, onClick = onSave)
    }
}

@Preview(showBackground = true)
@Composable
fun BottomSheetPreview() {
    DeepWorkTheme {
        AddCategoryContent(
            state = AddCategoryState(
                name = InputField("Learning Compose"),
                availableColors = colors,
                selectedColor = colors[2],
                isValid = true
            ),
            onEvent = {},
            onDismiss = {}
        )
    }
}

private val colors = listOf(
    Color.Red.toArgb(),
    Color.Blue.toArgb(),
    Color.Green.toArgb(),
    Color.Yellow.toArgb(),
    Color.Magenta.toArgb(),
    Color.Black.toArgb(),
    Color.Cyan.toArgb(),
    Color.White.toArgb()
)

