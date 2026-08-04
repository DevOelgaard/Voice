package voice.core.data.repo

import voice.core.data.PlaybackSession

public interface PlaybackSessionRepo {
  public suspend fun insert(session: PlaybackSession)
  public suspend fun getAll(): List<PlaybackSession>
  public fun flowAll(): kotlinx.coroutines.flow.Flow<List<PlaybackSession>>
  public suspend fun getForBook(bookId: String): List<PlaybackSession>
  public suspend fun deleteAll()
}
