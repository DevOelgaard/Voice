package voice.core.playback.analytics

import androidx.datastore.core.DataStore
import androidx.media3.common.Player
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import voice.core.data.BookId
import voice.core.data.PlaybackSession
import voice.core.data.repo.PlaybackSessionRepo

class PlaybackSessionTrackerTest {

  private lateinit var tracker: PlaybackSessionTracker
  private lateinit var repo: FakePlaybackSessionRepo
  private lateinit var dataStore: DataStore<BookId?>
  private lateinit var player: Player
  private val testScope = TestScope()

  @Before
  fun setUp() {
    repo = FakePlaybackSessionRepo()
    @Suppress("UNCHECKED_CAST")
    dataStore = mock(DataStore::class.java) as DataStore<BookId?>
    `when`(dataStore.data).thenReturn(flowOf(BookId("book1")))
    
    player = mock(Player::class.java)
    `when`(player.deviceVolume).thenReturn(10)

    tracker = PlaybackSessionTracker(repo, dataStore, testScope)
    tracker.attach(player)
  }

  @Test
  fun testSessionTracking() = testScope.runTest {
    var currentTime = 0L
    tracker.currentTimeMillis = { currentTime }

    tracker.onIsPlayingChanged(true)
    
    currentTime += 5000
    tracker.onDeviceVolumeChanged(15, false)
    
    currentTime += 5000
    tracker.onIsPlayingChanged(false)
    
    advanceUntilIdle()
    
    assertEquals(1, repo.sessions.size)
    val session = repo.sessions.first()
    assertEquals(BookId("book1"), session.bookId)
    assertEquals(10L, session.lengthInSeconds)
    
    assertEquals(2, session.volumeSpans.size)
    assertEquals(10, session.volumeSpans[0].volume)
    assertEquals(15, session.volumeSpans[1].volume)
    assertEquals(5000L, session.volumeSpans[0].durationMillis)
    assertEquals(5000L, session.volumeSpans[1].durationMillis)
    
    // (10 * 5000 + 15 * 5000) / 10000 = (50000 + 75000) / 10000 = 12.5f
    assertEquals(12.5f, session.averageVolume)
  }

  class FakePlaybackSessionRepo : PlaybackSessionRepo {
    val sessions = mutableListOf<PlaybackSession>()
    
    override suspend fun insert(session: PlaybackSession) {
      sessions.add(session)
    }

    override suspend fun getAll(): List<PlaybackSession> = sessions

    override suspend fun getForBook(bookId: String): List<PlaybackSession> = sessions.filter { it.bookId.value == bookId }

    override suspend fun deleteAll() {
      sessions.clear()
    }
  }
}
