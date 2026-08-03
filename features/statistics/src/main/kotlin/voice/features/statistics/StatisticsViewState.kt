package voice.features.statistics

import voice.core.data.BookId
import java.time.LocalDate
import androidx.compose.runtime.Immutable

@Immutable
public data class StatisticsViewState(
  val totalListeningSeconds: Long,
  val dailyStats: List<DailyStat>,
  val bookStats: List<BookStat>,
) {
  @Immutable
  public data class DailyStat(
    val date: LocalDate,
    val listeningSeconds: Long,
  )

  @Immutable
  public data class BookStat(
    val bookId: BookId,
    val bookTitle: String,
    val listeningSeconds: Long,
    val averageVolume: Float,
    val isExpanded: Boolean,
    val sessions: List<SessionDetail>
  )
  
  @Immutable
  public data class SessionDetail(
    val dateString: String,
    val durationSeconds: Long,
    val averageVolume: Float,
  )
}
