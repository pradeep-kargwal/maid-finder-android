package com.maidfinder.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.maidfinder.app.data.model.Job
import com.maidfinder.app.data.model.JobType
import com.maidfinder.app.ui.theme.GreenPrimary
import com.maidfinder.app.ui.viewmodel.JobFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaidDashboardScreen(
    viewModel: JobFeedViewModel? = null
) {
    val uiState = viewModel?.uiState?.collectAsState()?.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Job Feed",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState != null) {
                // Filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedJobType == null,
                        onClick = { viewModel?.setJobTypeFilter(null) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = uiState.selectedJobType == JobType.PART_TIME,
                        onClick = { viewModel?.setJobTypeFilter(JobType.PART_TIME) },
                        label = { Text("Part-time") }
                    )
                    FilterChip(
                        selected = uiState.selectedJobType == JobType.FULL_TIME,
                        onClick = { viewModel?.setJobTypeFilter(JobType.FULL_TIME) },
                        label = { Text("Full-time") }
                    )
                    FilterChip(
                        selected = uiState.selectedJobType == JobType.ONE_TIME,
                        onClick = { viewModel?.setJobTypeFilter(JobType.ONE_TIME) },
                        label = { Text("One-time") }
                    )
                }

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    uiState.jobs.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No jobs available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.jobs) { job ->
                                JobCard(job = job)
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Maid Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun JobCard(job: Job) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Job type badge
            Text(
                text = job.jobType.name.replace("_", "-").uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = GreenPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = job.title.ifEmpty { "House cleaning" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "\u20B9${job.budgetMin}-${job.budgetMax}/${job.budgetType.name.lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenPrimary
                )
                job.distanceKm?.let {
                    Text(
                        text = "$it km away",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${job.location.address} \u00B7 ${job.dateStart}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (job.applicantCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${job.applicantCount} applied",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            job.shifts.firstOrNull()?.let { shift ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${shift.type.name} ${shift.startTime}-${shift.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
