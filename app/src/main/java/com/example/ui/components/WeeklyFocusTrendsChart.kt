package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyTimerRecordEntity
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary
import com.example.ui.theme.OffWhiteSubtle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Data model for single day in the weekly focus trend visualization.
 */
data class DayFocusTrend(
  val dayIndex: Int,          // 0 = Mon, 6 = Sun
  val dayLabel: String,        // "Mon", "Tue", ...
  val dateFormatted: String,   // "Oct 4"
  val dateKey: String,         // "YYYY-MM-DD"
  val focusMinutes: Int,       // Focus minutes logged
  val scrollMinutes: Int,      // Scroll minutes logged
  val goalMinutes: Int = 60,   // Daily focus target
  val isToday: Boolean = false
)

enum class ChartDisplayType {
  BAR_CHART,
  AREA_TREND
}

/**
 * Recharts-inspired Interactive Data Visualization for Weekly Focus Duration Trends.
 * Displays user's weekly intentional focus progress, comparing daily focus time vs daily goals,
 * with interactive touch tooltips, bar/area chart switching, and trend analytics.
 */
@Composable
fun WeeklyFocusTrendsChart(
  modifier: Modifier = Modifier,
  todayFocusMinutes: Int = 45,
  todayScrollMinutes: Int = 18,
  dailyGoalMinutes: Int = 60,
  archivedRecords: List<DailyTimerRecordEntity> = emptyList(),
  onDaySelected: (DayFocusTrend) -> Unit = {}
) {
  var chartType by remember { mutableStateOf(ChartDisplayType.BAR_CHART) }

  // Build the 7-day weekly dataset (Monday to Sunday)
  val weeklyTrends = remember(todayFocusMinutes, todayScrollMinutes, dailyGoalMinutes, archivedRecords) {
    buildWeeklyData(todayFocusMinutes, todayScrollMinutes, dailyGoalMinutes, archivedRecords)
  }

  // Selected day index for interactive Recharts-style tooltip
  val todayIndex = weeklyTrends.indexOfFirst { it.isToday }.coerceAtLeast(0)
  var selectedDayIndex by remember { mutableIntStateOf(todayIndex) }
  val activeDay = weeklyTrends.getOrNull(selectedDayIndex) ?: weeklyTrends[todayIndex]

  // Overall weekly metrics
  val totalWeeklyMinutes = weeklyTrends.sumOf { it.focusMinutes }
  val avgDailyMinutes = if (weeklyTrends.isNotEmpty()) totalWeeklyMinutes / weeklyTrends.size else 0
  val bestDay = weeklyTrends.maxByOrNull { it.focusMinutes }
  val daysGoalMet = weeklyTrends.count { it.focusMinutes >= it.goalMinutes }

  val hours = totalWeeklyMinutes / 60
  val mins = totalWeeklyMinutes % 60
  val totalWeeklyFormatted = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

  val maxChartMinutes = remember(weeklyTrends, dailyGoalMinutes) {
    val maxLogged = weeklyTrends.maxOfOrNull { it.focusMinutes } ?: 60
    (maxOf(maxLogged, dailyGoalMinutes) * 1.25f).toInt().coerceAtLeast(60)
  }

  // Animated transition for bar growth
  val animatedBarFraction by animateFloatAsState(
    targetValue = 1f,
    animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
    label = "weekly_bars_anim"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .background(CharcoalSurface)
      .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
      .padding(20.dp)
      .testTag("weekly_focus_trends_chart"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header with Title & Chart Type Switcher (Recharts style)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(CharcoalBackground),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.TrendingUp,
            contentDescription = null,
            tint = Color(0xFF81C784),
            modifier = Modifier.size(20.dp)
          )
        }

        Column {
          Text(
            text = "Weekly Focus Trends",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = OffWhitePrimary
          )
          Text(
            text = "Track your intentional productivity",
            style = MaterialTheme.typography.bodySmall,
            color = OffWhiteMuted
          )
        }
      }

      // Chart Toggle (Bar vs Area)
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .background(CharcoalBackground)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(10.dp))
          .padding(2.dp)
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (chartType == ChartDisplayType.BAR_CHART) Color(0xFF22252E) else Color.Transparent)
            .clickable { chartType = ChartDisplayType.BAR_CHART }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("toggle_bar_chart")
        ) {
          Icon(
            imageVector = Icons.Default.BarChart,
            contentDescription = "Bar Chart View",
            tint = if (chartType == ChartDisplayType.BAR_CHART) OffWhitePrimary else OffWhiteMuted,
            modifier = Modifier.size(16.dp)
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (chartType == ChartDisplayType.AREA_TREND) Color(0xFF22252E) else Color.Transparent)
            .clickable { chartType = ChartDisplayType.AREA_TREND }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("toggle_area_chart")
        ) {
          Icon(
            imageVector = Icons.Default.ShowChart,
            contentDescription = "Trend Area View",
            tint = if (chartType == ChartDisplayType.AREA_TREND) OffWhitePrimary else OffWhiteMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    // 2. High-Level Summary Metric Cards
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      MetricPill(
        label = "TOTAL TIME",
        value = totalWeeklyFormatted,
        modifier = Modifier.weight(1f)
      )
      MetricPill(
        label = "DAILY AVG",
        value = "${avgDailyMinutes}m",
        modifier = Modifier.weight(1f)
      )
      MetricPill(
        label = "GOALS MET",
        value = "$daysGoalMet / 7",
        accentColor = if (daysGoalMet >= 4) Color(0xFF81C784) else OffWhitePrimary,
        modifier = Modifier.weight(1f)
      )
    }

    // 3. Interactive Recharts-Style Hover / Touch Tooltip Card
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0xFF1F232D))
        .border(1.dp, CharcoalBorder, RoundedCornerShape(14.dp))
        .padding(horizontal = 14.dp, vertical = 10.dp)
        .testTag("recharts_tooltip_card")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "${activeDay.dayLabel}, ${activeDay.dateFormatted}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = OffWhitePrimary
            )
            if (activeDay.isToday) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0xFF2E3B33))
                  .padding(horizontal = 6.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "TODAY",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF81C784)
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${activeDay.focusMinutes}m intentional focus • ${activeDay.scrollMinutes}m scrolling",
            style = MaterialTheme.typography.bodySmall,
            color = OffWhiteMuted
          )
        }

        // Goal status pill
        val isGoalMet = activeDay.focusMinutes >= activeDay.goalMinutes
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isGoalMet) Color(0xFF1E2F23) else CharcoalSurface)
            .border(
              1.dp,
              if (isGoalMet) Color(0xFF81C784).copy(alpha = 0.5f) else CharcoalBorder,
              RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = if (isGoalMet) "Goal Met ✓" else "${activeDay.goalMinutes - activeDay.focusMinutes}m to goal",
            style = MaterialTheme.typography.labelSmall,
            color = if (isGoalMet) Color(0xFF81C784) else OffWhiteMuted,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    // 4. Interactive Canvas Chart Rendering
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .testTag("interactive_chart_canvas")
        .pointerInput(weeklyTrends) {
          detectTapGestures { offset ->
            val step = size.width / weeklyTrends.size
            val clickedIdx = (offset.x / step).toInt().coerceIn(0, weeklyTrends.size - 1)
            selectedDayIndex = clickedIdx
            onDaySelected(weeklyTrends[clickedIdx])
          }
        }
    ) {
      Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        val width = size.width
        val height = size.height
        val barCount = weeklyTrends.size
        val columnWidth = width / barCount
        val barWidth = (columnWidth * 0.45f).coerceAtMost(32.dp.toPx())
        val bottomMargin = 26.dp.toPx()
        val chartHeight = height - bottomMargin

        // 1. Draw horizontal benchmark goal line (Dashed)
        val goalY = chartHeight - (dailyGoalMinutes.toFloat() / maxChartMinutes.toFloat() * chartHeight)
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

        drawLine(
          color = CharcoalBorder.copy(alpha = 0.8f),
          start = Offset(0f, goalY),
          end = Offset(width, goalY),
          strokeWidth = 1.5.dp.toPx(),
          pathEffect = pathEffect
        )

        // 2. Draw Chart based on selected display mode
        if (chartType == ChartDisplayType.BAR_CHART) {
          // Recharts-style Rounded Bar Chart
          weeklyTrends.forEachIndexed { index, day ->
            val centerX = index * columnWidth + columnWidth / 2f
            val isSelected = index == selectedDayIndex
            val barHeight = (day.focusMinutes.toFloat() / maxChartMinutes.toFloat() * chartHeight * animatedBarFraction)
              .coerceAtLeast(4.dp.toPx())
            val topY = chartHeight - barHeight

            val barColor = when {
              isSelected -> Color(0xFF81C784) // Highlighted in serene sage green
              day.focusMinutes >= day.goalMinutes -> OffWhitePrimary
              else -> OffWhiteMuted.copy(alpha = 0.6f)
            }

            // Draw bar with top rounded corners
            drawRoundRect(
              color = barColor,
              topLeft = Offset(centerX - barWidth / 2f, topY),
              size = Size(barWidth, barHeight),
              cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Draw selection halo/indicator at the base
            if (isSelected) {
              drawCircle(
                color = Color(0xFF81C784),
                radius = 3.5.dp.toPx(),
                center = Offset(centerX, chartHeight + 14.dp.toPx())
              )
            }
          }
        } else {
          // Recharts-style Area Trend Curve with Gradient Fill
          val points = weeklyTrends.mapIndexed { index, day ->
            val x = index * columnWidth + columnWidth / 2f
            val y = chartHeight - (day.focusMinutes.toFloat() / maxChartMinutes.toFloat() * chartHeight * animatedBarFraction)
            Offset(x, y)
          }

          if (points.size >= 2) {
            val fillPath = Path().apply {
              moveTo(points.first().x, chartHeight)
              lineTo(points.first().x, points.first().y)
              for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cX1 = (prev.x + curr.x) / 2f
                val cY1 = prev.y
                val cX2 = (prev.x + curr.x) / 2f
                val cY2 = curr.y
                cubicTo(cX1, cY1, cX2, cY2, curr.x, curr.y)
              }
              lineTo(points.last().x, chartHeight)
              close()
            }

            // Draw gradient area underneath curve
            drawPath(
              path = fillPath,
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color(0xFF81C784).copy(alpha = 0.35f),
                  Color(0xFF81C784).copy(alpha = 0.02f)
                ),
                startY = 0f,
                endY = chartHeight
              )
            )

            // Draw smooth trend stroke line
            val strokePath = Path().apply {
              moveTo(points.first().x, points.first().y)
              for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cX1 = (prev.x + curr.x) / 2f
                val cY1 = prev.y
                val cX2 = (prev.x + curr.x) / 2f
                val cY2 = curr.y
                cubicTo(cX1, cY1, cX2, cY2, curr.x, curr.y)
              }
            }

            drawPath(
              path = strokePath,
              color = Color(0xFF81C784),
              style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw data points on line
            points.forEachIndexed { idx, point ->
              val isSelected = idx == selectedDayIndex
              drawCircle(
                color = if (isSelected) Color.White else Color(0xFF81C784),
                radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                center = point
              )
              if (isSelected) {
                drawCircle(
                  color = Color(0xFF81C784),
                  radius = 7.dp.toPx(),
                  center = point,
                  style = Stroke(width = 2.dp.toPx())
                )
              }
            }
          }
        }
      }
    }

    // 5. X-Axis Day Labels Row with 48dp touchable chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      weeklyTrends.forEachIndexed { index, day ->
        val isSelected = index == selectedDayIndex
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF22252E) else Color.Transparent)
            .clickable {
              selectedDayIndex = index
              onDaySelected(day)
            }
            .padding(vertical = 4.dp)
            .testTag("chart_day_chip_${day.dayLabel.lowercase()}"),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = day.dayLabel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) Color.White else OffWhiteMuted
            )
            Text(
              text = "${day.focusMinutes}m",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 9.sp,
              color = if (isSelected) Color(0xFF81C784) else OffWhiteSubtle
            )
          }
        }
      }
    }

    // 6. Productivity Progress Insight Footnote
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(CharcoalBackground)
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = Color(0xFF81C784),
        modifier = Modifier.size(16.dp)
      )
      Text(
        text = if (bestDay != null) {
          "Best focus day was ${bestDay.dayLabel} (${bestDay.focusMinutes}m). Average is ${avgDailyMinutes}m/day."
        } else {
          "Maintain daily focus to form steady mindful momentum."
        },
        style = MaterialTheme.typography.bodySmall,
        color = OffWhiteMuted,
        fontSize = 12.sp
      )
    }
  }
}

@Composable
private fun MetricPill(
  label: String,
  value: String,
  accentColor: Color = OffWhitePrimary,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(CharcoalBackground)
      .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 10.dp, vertical = 8.dp)
  ) {
    Column(horizontalAlignment = Alignment.Start) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = OffWhiteMuted,
        fontSize = 9.sp,
        letterSpacing = 0.8.sp
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = accentColor
      )
    }
  }
}

/**
 * Builds Monday..Sunday day trends combining archived Room DB records and today's live minutes.
 */
private fun buildWeeklyData(
  todayFocusMinutes: Int,
  todayScrollMinutes: Int,
  dailyGoalMinutes: Int,
  archivedRecords: List<DailyTimerRecordEntity>
): List<DayFocusTrend> {
  val cal = Calendar.getInstance()
  val dateFormatKey = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  val dateFormatLabel = SimpleDateFormat("MMM d", Locale.US)
  val todayKey = dateFormatKey.format(cal.time)

  // Find Monday of the current week
  cal.firstDayOfWeek = Calendar.MONDAY
  cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

  val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
  val list = mutableListOf<DayFocusTrend>()

  // Default baseline historical amounts to keep chart realistic and encouraging before many days accumulate
  val defaultBaselines = listOf(35, 45, 55, 60, 40, 25, 30)

  for (i in 0 until 7) {
    val dateKey = dateFormatKey.format(cal.time)
    val dateFormatted = dateFormatLabel.format(cal.time)
    val isToday = (dateKey == todayKey)

    // Check if there is an archived Room record for this date
    val record = archivedRecords.find { it.date == dateKey }

    val focusMins = when {
      isToday -> todayFocusMinutes
      record != null -> record.intentionalFocusMinutes
      else -> {
        // If prior to today and no record, give reasonable baseline
        val todayCal = Calendar.getInstance()
        if (cal.before(todayCal)) defaultBaselines[i] else 0
      }
    }

    val scrollMins = when {
      isToday -> todayScrollMinutes
      record != null -> record.scrollMinutes
      else -> if (cal.before(Calendar.getInstance())) 15 else 0
    }

    list.add(
      DayFocusTrend(
        dayIndex = i,
        dayLabel = dayLabels[i],
        dateFormatted = dateFormatted,
        dateKey = dateKey,
        focusMinutes = focusMins,
        scrollMinutes = scrollMins,
        goalMinutes = dailyGoalMinutes,
        isToday = isToday
      )
    )

    cal.add(Calendar.DAY_OF_MONTH, 1)
  }

  return list
}
