package voice.core.data.repo

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import voice.core.data.PlaybackSession
import voice.core.data.repo.internals.dao.PlaybackSessionDao

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
public class PlaybackSessionRepoImpl(
  private val dao: PlaybackSessionDao,
) : PlaybackSessionRepo {
  override suspend fun insert(session: PlaybackSession) {
    dao.insert(session)
  }

  override suspend fun getAll(): List<PlaybackSession> {
    return dao.getAll()
  }

  override suspend fun getForBook(bookId: String): List<PlaybackSession> {
    return dao.getForBook(bookId)
  }

  override suspend fun deleteAll() {
    dao.deleteAll()
  }
}
