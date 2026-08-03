package voice.features.statistics

import voice.core.data.BookId

public interface StatisticsListener {
  public fun close()
  public fun toggleBookExpanded(bookId: BookId)
}
