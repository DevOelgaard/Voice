package voice.features.statistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Graph
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Module
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.core.ui.NavEntry
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.core.ui.retain

@ContributesTo(AppScope::class)
public interface StatisticsGraph {
  public val statisticsViewModel: StatisticsViewModel
}

@ContributesTo(AppScope::class)
@Module
public interface StatisticsModule {
  @Provides
  @IntoSet
  public fun statisticsNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Statistics> { key ->
    NavEntry(key) {
      StatisticsScreen()
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun StatisticsScreen() {
  val viewModel = retain<StatisticsViewModel> { rootGraphAs<StatisticsGraph>().statisticsViewModel }
  val state = viewModel.viewState()
  val listener: StatisticsListener = viewModel
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Statistics") },
        navigationIcon = {
          IconButton(onClick = { listener.close() }) {
             Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        TotalListeningTimeCard(state.totalListeningSeconds)
      }

      item {
        DailyListeningChart(state.dailyStats)
      }

      item {
        Text(
          text = "Listening by Book",
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
      }

      items(state.bookStats, key = { it.bookId.value }) { bookStat ->
        BookStatCard(bookStat = bookStat, onToggle = { listener.toggleBookExpanded(bookStat.bookId) })
      }
    }
  }
}

@Composable
private fun TotalListeningTimeCard(totalSeconds: Long) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(16.dp).fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = "Total Listening Time", style = MaterialTheme.typography.titleMedium)
      Spacer(modifier = Modifier.height(8.dp))
      val hours = totalSeconds / 3600
      val minutes = (totalSeconds % 3600) / 60
      Text(
        text = "${hours}h ${minutes}m",
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.primary
      )
    }
  }
}

@Composable
private fun DailyListeningChart(dailyStats: List<StatisticsViewState.DailyStat>) {
  if (dailyStats.isEmpty()) return

  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(text = "Daily Listening", style = MaterialTheme.typography.titleMedium)
      Spacer(modifier = Modifier.height(16.dp))

      val maxSeconds = dailyStats.maxOfOrNull { it.listeningSeconds }?.coerceAtLeast(1) ?: 1
      
      var startAnimation by remember { mutableStateOf(false) }
      LaunchedEffect(Unit) {
        startAnimation = true
      }

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().height(150.dp)
      ) {
        items(dailyStats) { stat ->
          val targetRatio = (stat.listeningSeconds.toFloat() / maxSeconds.toFloat()).coerceIn(0f, 1f)
          val animatedHeightRatio by animateFloatAsState(
            targetValue = if (startAnimation) targetRatio else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "barHeight"
          )

          val barColor = MaterialTheme.colorScheme.primary

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxHeight()
          ) {
            Canvas(modifier = Modifier.width(16.dp).weight(1f)) {
              val canvasHeight = size.height
              val barHeight = canvasHeight * animatedHeightRatio
              drawRoundRect(
                color = barColor,
                topLeft = Offset(0f, canvasHeight - barHeight),
                size = Size(size.width, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = stat.date.format(DateTimeFormatter.ofPattern("dd")),
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BookStatCard(
  bookStat: StatisticsViewState.BookStat,
  onToggle: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onToggle)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = bookStat.bookTitle,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.weight(1f)
        )
        val hours = bookStat.listeningSeconds / 3600
        val minutes = (bookStat.listeningSeconds % 3600) / 60
        Text(
          text = "${hours}h ${minutes}m",
          style = MaterialTheme.typography.bodyMedium
        )
      }

      AnimatedVisibility(visible = bookStat.isExpanded) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
          Text(text = "Average Volume: ${(bookStat.averageVolume).toInt()}%", style = MaterialTheme.typography.bodySmall)
          Spacer(modifier = Modifier.height(8.dp))
          
          if (bookStat.sessions.isNotEmpty()) {
            Text(text = "Recent Sessions", style = MaterialTheme.typography.labelMedium)
            bookStat.sessions.take(5).forEach { session ->
              Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = session.dateString, style = MaterialTheme.typography.bodySmall)
                val m = session.durationSeconds / 60
                val s = session.durationSeconds % 60
                Text(text = "${m}m ${s}s", style = MaterialTheme.typography.bodySmall)
              }
            }
          }
        }
      }
    }
  }
}
