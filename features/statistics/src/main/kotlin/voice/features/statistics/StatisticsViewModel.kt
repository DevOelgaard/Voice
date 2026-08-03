package voice.features.statistics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import voice.core.data.BookId
import voice.core.data.repo.BookRepository
import voice.core.data.repo.PlaybackSessionRepo
import voice.navigation.Navigator
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Inject
public class StatisticsViewModel(
  private val navigator: Navigator,
  private val playbackSessionRepo: PlaybackSessionRepo,
  private val bookRepository: BookRepository,
) : StatisticsListener {

  private val expandedBooks = mutableStateOf<Set<BookId>>(emptySet())

  @Composable
  public fun viewState(): StatisticsViewState {
    val sessions by remember { playbackSessionRepo.getAll() }.collectAsState(initial = emptyList())
    val books by remember { bookRepository.flow() }.collectAsState(initial = emptyList())
    
    val bookMap = remember(books) { books.associateBy { it.id } }

    val totalSeconds = remember(sessions) {
      sessions.sumOf { it.lengthInSeconds }
    }

    val dailyStats = remember(sessions) {
      sessions.groupBy { it.startedAt.atZone(ZoneId.systemDefault()).toLocalDate() }
        .map { (date, dailySessions) ->
          StatisticsViewState.DailyStat(
            date = date,
            listeningSeconds = dailySessions.sumOf { it.lengthInSeconds }
          )
        }
        .sortedBy { it.date }
    }

    val bookStats = remember(sessions, bookMap, expandedBooks.value) {
      sessions.groupBy { it.bookId }
        .map { (bookId, bookSessions) ->
          val bookTitle = bookMap[bookId]?.title ?: "Unknown Book"
          val totalTime = bookSessions.sumOf { it.lengthInSeconds }
          
          val totalVolumeTime = bookSessions.sumOf { s -> 
            s.volumeSpans.sumOf { it.durationMillis } 
          }
          val averageVolume = if (totalVolumeTime > 0) {
             bookSessions.sumOf { s ->
               s.volumeSpans.sumOf { it.volume.toLong() * it.durationMillis }.toFloat()
             } / totalVolumeTime
          } else {
             0f
          }
          
          val isExpanded = expandedBooks.value.contains(bookId)
          val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
          val sessionDetails = if (isExpanded) {
            bookSessions.sortedByDescending { it.startedAt }.map { s ->
              StatisticsViewState.SessionDetail(
                dateString = s.startedAt.atZone(ZoneId.systemDefault()).format(formatter),
                durationSeconds = s.lengthInSeconds,
                averageVolume = s.averageVolume
              )
            }
          } else emptyList()

          StatisticsViewState.BookStat(
            bookId = bookId,
            bookTitle = bookTitle,
            listeningSeconds = totalTime,
            averageVolume = averageVolume,
            isExpanded = isExpanded,
            sessions = sessionDetails
          )
        }
        .sortedByDescending { it.listeningSeconds }
    }

    return StatisticsViewState(
      totalListeningSeconds = totalSeconds,
      dailyStats = dailyStats,
      bookStats = bookStats,
    )
  }

  override fun close() {
    navigator.goBack()
  }

  override fun toggleBookExpanded(bookId: BookId) {
    val current = expandedBooks.value
    expandedBooks.value = if (current.contains(bookId)) {
      current - bookId
    } else {
      current + bookId
    }
  }
}
