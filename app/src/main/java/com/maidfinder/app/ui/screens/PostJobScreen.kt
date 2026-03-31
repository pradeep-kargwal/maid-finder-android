package com.maidfinder.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maidfinder.app.domain.model.BudgetType
import com.maidfinder.app.domain.model.JobType
import com.maidfinder.app.domain.model.ShiftType
import com.maidfinder.app.ui.theme.GreenPrimary
import com.maidfinder.app.ui.viewmodel.PostJobViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostJobScreen(
    viewModel: PostJobViewModel,
    onBackClick: () -> Unit,
    onJobPosted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isSubmitted) {
        JobPostedSuccess(onDone = onJobPosted)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post a Job", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Job Type
            SectionLabel("Job Type *")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                JobType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = uiState.jobType == type,
                        onClick = { viewModel.updateJobType(type) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = JobType.entries.size
                        )
                    ) {
                        Text(type.name.replace("_", "-"))
                    }
                }
            }

            // Title
            SectionLabel("Title (optional)")
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                placeholder = { Text("e.g. Daily house cleaning") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            SectionLabel("Description (optional)")
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                placeholder = { Text("Describe the work needed...") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4
            )

            // Location
            SectionLabel("Location")
            OutlinedTextField(
                value = uiState.location,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false,
                label = { Text("Auto-detected") }
            )

            // Date Start
            SectionLabel("Start Date *")
            OutlinedTextField(
                value = uiState.dateStart,
                onValueChange = { viewModel.updateDateStart(it) },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Date End
            SectionLabel("End Date (optional)")
            OutlinedTextField(
                value = uiState.dateEnd,
                onValueChange = { viewModel.updateDateEnd(it) },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Shift
            SectionLabel("Shift *")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShiftType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.shiftType == type,
                        onClick = { viewModel.updateShiftType(type) },
                        label = {
                            Text(
                                when (type) {
                                    ShiftType.MORNING -> "\uD83C\uDF05 AM 6-10"
                                    ShiftType.AFTERNOON -> "\u2600\uFE0F PM 12-4"
                                    ShiftType.EVENING -> "\uD83C\uDF19 EVE 4-8"
                                    ShiftType.CUSTOM -> "\u23F0 Custom"
                                }
                            )
                        }
                    )
                }
            }

            // Budget
            SectionLabel("Budget *")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.budgetMin,
                    onValueChange = { viewModel.updateBudgetMin(it) },
                    label = { Text("Min") },
                    prefix = { Text("\u20B9") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.budgetMax,
                    onValueChange = { viewModel.updateBudgetMax(it) },
                    label = { Text("Max") },
                    prefix = { Text("\u20B9") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Budget Type
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BudgetType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.budgetType == type,
                        onClick = { viewModel.updateBudgetType(type) },
                        label = { Text(type.name.lowercase()) }
                    )
                }
            }

            // Error message
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit button
            Button(
                onClick = { viewModel.submitJob() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }
                Text(
                    if (uiState.isSubmitting) "Posting..." else "Post Job \uD83D\uDE80",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun JobPostedSuccess(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83C\uDF89",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Job Posted!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = GreenPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Maids nearby will be notified",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
        ) {
            Text("Done")
        }
    }
}
