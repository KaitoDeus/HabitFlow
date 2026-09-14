package com.habitflow.app

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.time.LocalDate

data class HabitWidgetItem(
    val id: String,
    val name: String,
    val completed: Boolean
)

class ToggleHabitActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[habitIdKey] ?: return
        val app = context.applicationContext as HabitFlowApplication
        val repository = app.repository
        val database = app.database
        val sharedPrefs = context.getSharedPreferences("habitflow_test_prefs", Context.MODE_PRIVATE)
        val offset = sharedPrefs.getLong("test_date_offset", 0L)

        val today = LocalDate.now().plusDays(offset)
        val todayEpochDay = today.toEpochDay()

        val occurrences = repository.getOccurrencesDirect()
        val current = occurrences.firstOrNull { it.habitId == habitId && it.scheduledEpochDay == todayEpochDay }

        if (current?.status == OccurrenceStatus.COMPLETED) {
            repository.unmark(habitId, todayEpochDay)
        } else {
            repository.mark(habitId, OccurrenceStatus.COMPLETED, dateEpochDay = todayEpochDay)

            val habits = repository.getActiveHabitsDirect()
            val updatedOccurrences = repository.getOccurrencesDirect()
            val stats = HabitStatisticsCalculator.calculate(updatedOccurrences, habits, todayEpochDay)
            GamificationManager.processCompletion(repository, stats.currentStreak, todayEpochDay)

            val goals = database.goalDao().all()
            goals.filter { it.linkedHabitId == habitId }.forEach { goal ->
                repository.addGoalProgress(goal, goal.contributionValue)
            }
        }

        HabitWidget().update(context, glanceId)
        try {
            val widgetIntent = Intent("android.appwidget.action.APPWIDGET_UPDATE").apply {
                `package` = context.packageName
            }
            context.sendBroadcast(widgetIntent)
        } catch (_: Exception) {}
    }

    companion object {
        val habitIdKey = ActionParameters.Key<String>("habit_id")
    }
}

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidget()
}

class HabitWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as HabitFlowApplication).repository
        val sharedPrefs = context.getSharedPreferences("habitflow_test_prefs", Context.MODE_PRIVATE)
        val offset = sharedPrefs.getLong("test_date_offset", 0L)

        // Fetch data for today directly from the database to avoid race conditions with Room flow caching
        val habits = repository.getActiveHabitsDirect()
        val occurrences = repository.getOccurrencesDirect()

        val today = LocalDate.now().plusDays(offset)
        val todayEpochDay = today.toEpochDay()
        val dayOfWeek = today.dayOfWeek.value

        val todayHabits = habits.filter { habit ->
            val isScheduled = habit.scheduledDays.isEmpty() || habit.scheduledDays.split(",").contains(dayOfWeek.toString())
            val isCreated = (habit.createdAt / 86400000L) <= todayEpochDay
            isScheduled && isCreated
        }

        val todayOccurrences = occurrences.filter { it.scheduledEpochDay == todayEpochDay }.associateBy { it.habitId }

        val completedCount = todayHabits.count { todayOccurrences[it.id]?.status == OccurrenceStatus.COMPLETED }
        val totalCount = todayHabits.size
        val percentage = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

        val widgetItems = todayHabits.map { habit ->
            HabitWidgetItem(
                id = habit.id,
                name = habit.name,
                completed = todayOccurrences[habit.id]?.status == OccurrenceStatus.COMPLETED
            )
        }

        provideContent {
            WidgetContent(percentage, widgetItems)
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetContent(percentage: Int, habits: List<HabitWidgetItem>) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Progress Area (Click to open app)
            Column(
                modifier = GlanceModifier
                    .width(80.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(60.dp)
                        .background(Color(0xFF39FF14))
                        .cornerRadius(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$percentage%",
                        style = TextStyle(
                            color = ColorProvider(Color.Black),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = "Hoàn thành",
                    style = TextStyle(color = ColorProvider(Color.Gray), fontSize = 10.sp)
                )
            }

            Spacer(GlanceModifier.width(12.dp))

            // Right: Habit List
            Column(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thói quen hôm nay",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF39FF14)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(GlanceModifier.height(6.dp))
                if (habits.isEmpty()) {
                    Text(
                        text = "Không có thói quen nào",
                        style = TextStyle(color = ColorProvider(Color.LightGray), fontSize = 11.sp),
                        modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>())
                    )
                } else {
                    val visibleHabits = habits.take(5)
                    Column(modifier = GlanceModifier.defaultWeight().fillMaxWidth()) {
                        visibleHabits.forEachIndexed { index, habitItem ->
                            Row(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .fillMaxWidth()
                                    .background(if (habitItem.completed) Color(0x3339FF14) else Color(0xFF2C2C2E))
                                    .cornerRadius(8.dp)
                                    .padding(horizontal = 8.dp)
                                    .clickable(
                                        actionRunCallback<ToggleHabitActionCallback>(
                                            actionParametersOf(ToggleHabitActionCallback.habitIdKey to habitItem.id)
                                        )
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (habitItem.completed) "✓" else "○",
                                    style = TextStyle(
                                        color = ColorProvider(if (habitItem.completed) Color(0xFF39FF14) else Color.White),
                                        fontSize = 11.sp
                                    )
                                )
                                Spacer(GlanceModifier.width(6.dp))
                                Text(
                                    text = habitItem.name,
                                    maxLines = 1,
                                    style = TextStyle(
                                        color = ColorProvider(if (habitItem.completed) Color.Gray else Color.White),
                                        fontSize = 12.sp,
                                        fontWeight = if (habitItem.completed) FontWeight.Normal else FontWeight.Medium
                                    )
                                )
                            }
                            if (index < visibleHabits.size - 1) {
                                Spacer(GlanceModifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
