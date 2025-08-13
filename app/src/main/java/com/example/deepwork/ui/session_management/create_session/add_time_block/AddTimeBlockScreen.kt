package com.example.deepwork.ui.session_management.create_session.add_time_block

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.compose.DeepWorkTheme
import com.example.deepwork.R
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.ui.components.ActionButton
import com.example.deepwork.ui.components.SecondaryButton
import com.example.deepwork.ui.components.TextField
import com.example.deepwork.ui.model.InputField
import com.example.deepwork.ui.session_management.create_session.add_time_block.add_category.AddCategoryBottomSheet
import com.example.deepwork.ui.util.ObserveAsEvents
import com.example.deepwork.ui.util.UiEvent

@Composable
fun AddTimeBlockScreen(
    viewModel: AddTimeBlockViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onNavigate: (UiEvent.Navigate) -> Unit,
    onNavigateUp: () -> Unit
) {
    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is UiEvent.Navigate -> onNavigate(event)
            is UiEvent.NavigateUp -> onNavigateUp()
            is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
        }
    }
    AddTimeBlockContent(
        state = viewModel.state,
        onNavigate = onNavigate,
        onNavigateUp = onNavigateUp,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimeBlockContent(
    state: AddTimeBlockState,
    onNavigate: (UiEvent.Navigate) -> Unit,
    onNavigateUp: () -> Unit,
    onEvent: (AddTimeBlockEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Add Block") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (state.showAddCategoryBottomSheet) {
                AddCategoryBottomSheet(
                    onDismiss = { onEvent(AddTimeBlockEvent.AddCategoryBottomSheetDismissed) },
                    snackbarHostState = SnackbarHostState()
                )
            }
            TimeBlockSelectionChips(
                state = state,
                onEvent = onEvent
            )
            DurationTextField(
                state = state,
                onDurationChanged = { onEvent(AddTimeBlockEvent.DurationChanged(it)) }
            )
            val shouldShowCategories = state.selectedBlockType != ScheduledTimeBlock.BlockType.BREAK
            if (shouldShowCategories) {
                CategoriesComponent(
                    state = state,
                    onEvent = onEvent,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            AddTimeBlockButtons(
                state = state,
                onEvent = onEvent,
            )
            if (state.showConfirmCancelDialog) {
                ConfirmCancelDialog(
                    onConfirm = { onEvent(AddTimeBlockEvent.ConfirmCancelClicked) },
                    onDismiss = { onEvent(AddTimeBlockEvent.DismissCancelClicked)}
                )
            }
        }
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
            isSelected = state.selectedBlockType == ScheduledTimeBlock.BlockType.DEEP_WORK,
            color = MaterialTheme.colorScheme.primary,
            onClick = { onEvent(AddTimeBlockEvent.BlockTypeSelected(ScheduledTimeBlock.BlockType.DEEP_WORK)) }
        )
        TimeBlockChip(
            title = "Shallow Work",
            isSelected = state.selectedBlockType == ScheduledTimeBlock.BlockType.SHALLOW_WORK,
            color = colorResource(R.color.timeblock_shallowwork),
            onClick = { onEvent(AddTimeBlockEvent.BlockTypeSelected(ScheduledTimeBlock.BlockType.SHALLOW_WORK)) }
        )
        TimeBlockChip(
            title = "Break",
            isSelected = state.selectedBlockType == ScheduledTimeBlock.BlockType.BREAK,
            color = colorResource(R.color.timeblock_break),
            onClick = { onEvent(AddTimeBlockEvent.BlockTypeSelected(ScheduledTimeBlock.BlockType.BREAK)) }
        )
    }
}

@Composable
fun DurationTextField(
    state: AddTimeBlockState,
    onDurationChanged: (String) -> Unit
) {
    val duration = state.duration
    TextField(
        value = duration.value.ifBlank { "" },
        onValueChange = onDurationChanged,
        placeholder = duration.placeHolder ?: "",
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

@Composable
fun SelectableCategory(
    category: Category,
    isSelected: Boolean,
    onSelected: (Category) -> Unit
) {
    val alpha = if (isSelected) 0.3f else 1f
    InputChip(
        label = { Text(
            text = category.name,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .padding(start = 8.dp, top = 4.dp, end = 4.dp, bottom = 4.dp)
                .alpha(alpha)
        ) },
        selected = isSelected,
        colors = InputChipDefaults.inputChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha),
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = InputChipDefaults.inputChipBorder(
            enabled = !isSelected,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = { onSelected(category) },
        leadingIcon = {
            CategoryCircleIcon(
                colorHex = category.colorHex,
                alpha = alpha
            )
        },
    )
}

@Composable
fun AddCategoryChipButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InputChip(
        label = { Text(
            text = "Create New",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        ) },
        selected = false,
        onClick = onClick,
        colors = InputChipDefaults.inputChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = InputChipDefaults.inputChipBorder(
            enabled = true,
            selected = false,
            borderColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
        leadingIcon = {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add icon",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    )
}

@Composable
fun SelectedCategory(
    category: Category,
    onUnselected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    InputChip(
        label = { Text(
            text = category.name,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        ) },
        selected = false,
        onClick = { },
        colors = InputChipDefaults.inputChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = InputChipDefaults.inputChipBorder(
            enabled = true,
            selected = false,
            borderColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp),
        trailingIcon = {
            IconButton(
                onClick = { onUnselected(category) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Done icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp),
                )
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun SelectedCategoryPreview() {
    DeepWorkTheme {
        SelectedCategory(
            category = Category.create("Coding", Color.Blue.toArgb()),
            onUnselected = { }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectedCategories(
    categories: List<Category>,
    onUnselected: (Category) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        categories.forEach { category ->
            SelectedCategory(
                category = category,
                onUnselected = onUnselected
            )
        }
    }
}

@Composable
fun CategoryCircleIcon(
    colorHex: Int,
    alpha: Float = 1f,
    modifier: Modifier = Modifier
) {
    val circleBorderColor = MaterialTheme.colorScheme.onPrimaryContainer
    Canvas(modifier = modifier
        .size(4.dp)
        .padding(start = 8.dp, end = 4.dp)
    ) {
        drawCircle(
            color = circleBorderColor.copy(alpha = alpha),
            radius = 7.dp.toPx(),
        )
        drawCircle(
            color = Color(colorHex).copy(alpha = alpha),
            radius = 6.dp.toPx(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectableCategories(
    categories: List<SelectableCategory>,
    onSelected: (Category) -> Unit,
    onAddNewClicked: () -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AddCategoryChipButton(
            onClick = onAddNewClicked,
        )
        categories.forEach { category ->
            SelectableCategory(
                category = category.category,
                isSelected = category.isSelected,
                onSelected = onSelected
            )
        }
    }
}

@Composable
fun CategoriesComponent(
    state: AddTimeBlockState,
    onEvent: (AddTimeBlockEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Note that this Column's properties are the same as those in AddTimeBlockContent
    // If anything changes there it should be reflected here
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(4.dp))
        Text("Add Categories",
            style = MaterialTheme.typography.titleMedium,)
        Text("Selected (${state.selectedCategoriesCount}/${ScheduledTimeBlock.CATEGORIES_MAX})")
        SelectedCategories(
            categories = state.categories.filter { it.isSelected }.map { it.category },
            onUnselected = { onEvent(AddTimeBlockEvent.CategoryUnselected(it)) }
        )
        Text("Available Categories")
        SelectableCategories(
            categories = state.categories,
            onSelected = { onEvent(AddTimeBlockEvent.CategorySelected(it)) },
            onAddNewClicked = { onEvent(AddTimeBlockEvent.CreateCategoryClicked) })
    }
}

@Preview(showBackground = true)
@Composable
fun AddTimeBlockContentPreview() {
    DeepWorkTheme {
        AddTimeBlockContent(
            state = AddTimeBlockState(
                selectedBlockType = ScheduledTimeBlock.BlockType.DEEP_WORK,
                duration = InputField(
                    value = "",
                    placeHolder = "25 to 120 minutes"
                ),
                categories = testCategories(),
                isValid = true,
                showConfirmCancelDialog = false
            ),
            onNavigate = {},
            onNavigateUp = {},
            onEvent = {}
        )
    }
}

@Composable
fun ConfirmCancelDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        title = {
            Text(text = stringResource(R.string.cancel_create_block_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.cancel_create_block_dialog_message))
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm() }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text("Dismiss")
            }
        },
        modifier = modifier
    )
}

private fun testCategories(): List<SelectableCategory> {
    return listOf(
        Category.create("Coding", Color.Blue.toArgb()),
        Category.create("Design", Color.Red.toArgb()),
        Category.create("Writing", Color.Green.toArgb()),
        Category.create("Research", Color.Yellow.toArgb()),
        Category.create("Learning", Color.Magenta.toArgb()),
    ).map { category ->
        val isSelected = category.name == "Coding" || category.name == "Learning"
        SelectableCategory(category, isSelected)
    }
}