

package com.habitflow.app.feature.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Đang tải thống kê...")
                Spacer(Modifier.padding(4.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        uiState.error != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Không thể tải thống kê",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.padding(4.dp))

                Text(uiState.error ?: "")
            }
        }

        else -> {
            StatisticsContent(uiState)
        }
    }
}

@Composable
private fun StatisticsContent(
    state: StatisticsUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Thống kê",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Tỷ lệ hoàn thành: ${
                "%.1f".format(state.completionRate)
            }%",
            style = MaterialTheme.typography.titleMedium
        )

        LinearProgressIndicator(
            progress = {
                (state.completionRate / 100.0)
                    .toFloat()
                    .coerceIn(0f, 1f)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                "Hoàn thành",
                state.completed.toString(),
                Modifier.weight(1f)
            )

            StatCard(
                "Bỏ lỡ",
                state.missed.toString(),
                Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                "Bỏ qua",
                state.skipped.toString(),
                Modifier.weight(1f)
            )

            StatCard(
                "Đóng băng",
                state.frozen.toString(),
                Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                "Chuỗi hiện tại",
                state.currentStreak.toString(),
                Modifier.weight(1f)
            )

            StatCard(
                "Chuỗi dài nhất",
                state.longestStreak.toString(),
                Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                "Tỷ lệ tuần",
                "${state.weeklyCompletionRate.toInt()}%",
                Modifier.weight(1f)
            )

            StatCard(
                "Tỷ lệ tháng",
                "${state.monthlyCompletionRate.toInt()}%",
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}