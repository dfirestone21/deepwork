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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.compose.DeepWorkTheme
import com.example.deepwork.R
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.ui.components.ActionButton
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
            TimeBlockSelectionChips(
                state = state,
                onEvent = onEvent
            )
            DurationTextField(
                state = state,
                onDurationChanged = { onEvent(AddTimeBlockEvent.DurationChanged(it)) }
            )
            val shouldShowCategories = state.selectedBlockType != TimeBlock.BlockType.BREAK
            if (shouldShowCategories) {
                CategoriesComponent(
                    state = state,
                    onEvent = onEvent,
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
            category = Category("1", "Coding", Color.Blue.toArgb()),
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
    onSelected: (Category) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
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
fun SelectableCategoriesPreview() {
    DeepWorkTheme {
        SelectableCategories(
            categories = testCategories(),
            onSelected = { }
        )
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
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(4.dp))
        Text("Add Categories",
            style = MaterialTheme.typography.titleMedium,)
        Text("Selected (${state.selectedCategoriesCount}/${state.maxSelectableCategories})")
        SelectedCategories(
            categories = state.categories.filter { it.isSelected }.map { it.category },
            onUnselected = { onEvent(AddTimeBlockEvent.CategoryUnselected(it)) }
        )
        Text("Available Categories")
        SelectableCategories(
            categories = state.categories,
            onSelected = { onEvent(AddTimeBlockEvent.CategorySelected(it)) })
        Spacer(modifier = Modifier.weight(1f))
        AddTimeBlockButtons(
            state = state,
            onEvent = onEvent,
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
                duration = InputField(
                    value = "",
                    placeHolder = "25 to 120 minutes"
                ),
                categories = testCategories(),
                isValid = true
            ),
            onNavigate = {},
            onNavigateUp = {},
            onEvent = {}
        )
    }
}

private fun testCategories(): List<SelectableCategory> {
    return listOf(
        Category("1", "Coding", Color.Blue.toArgb()),
        Category("2", "Design", Color.Red.toArgb()),
        Category("3", "Writing", Color.Green.toArgb()),
        Category("4", "Research", Color.Yellow.toArgb()),
        Category("5", "Learning", Color.Magenta.toArgb()),
    ).map { category ->
        val isSelected = category.id == "1" || category.id == "5"
        SelectableCategory(category, isSelected)
    }
}