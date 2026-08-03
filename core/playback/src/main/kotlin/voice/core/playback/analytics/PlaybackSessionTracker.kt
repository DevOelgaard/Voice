package voice.core.playback.analytics

import androidx.datastore.core.DataStore
import androidx.media3.common.Player
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import voice.core.data.BookId
import voice.core.data.PlaybackSession
import voice.core.data.VolumeSpan
import voice.core.data.repo.PlaybackSessionRepo
import voice.core.data.store.CurrentBookStore
import java.time.Instant

@Inject
@SingleIn(AppScope::class)
public class PlaybackSessionTracker(
  private val sessionRepo: PlaybackSessionRepo,
  @CurrentBookStore
  private val currentBookStore: DataStore<BookId?>,
  private val scope: CoroutineScope,
) : Player.Listener {

  private var player: Player? = null
  private var sessionStartTime: Instant? = null
  private var currentVolume: Int = 0
  private var volumeSpanStartTime: Long = 0
  private val volumeSpans = mutableListOf<VolumeSpan>()

  internal var currentTimeMillis: () -> Long = { System.currentTimeMillis() }

  public fun attach(player: Player) {
    this.player = player
    this.currentVolume = player.deviceVolume
    player.addListener(this)
  }

  override fun onIsPlayingChanged(isPlaying: Boolean) {
    if (isPlaying) {
      startSession()
    } else {
      endSession()
    }
  }

  override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
    if (sessionStartTime != null) {
      val now = currentTimeMillis()
      val durationMillis = now - volumeSpanStartTime
      if (durationMillis > 0) {
        volumeSpans.add(VolumeSpan(currentVolume, durationMillis))
      }
      currentVolume = volume
      volumeSpanStartTime = now
    } else {
      currentVolume = volume
    }
  }

  private fun startSession() {
    sessionStartTime = Instant.now()
    volumeSpanStartTime = currentTimeMillis()
    volumeSpans.clear()
    player?.let {
      currentVolume = it.deviceVolume
    }
  }

  private fun endSession() {
    val start = sessionStartTime ?: return
    val now = currentTimeMillis()
    val finalSpanDuration = now - volumeSpanStartTime
    if (finalSpanDuration > 0) {
      volumeSpans.add(VolumeSpan(currentVolume, finalSpanDuration))
    }

    val lengthMillis = volumeSpans.sumOf { it.durationMillis }
    val lengthInSeconds = lengthMillis / 1000

    if (lengthInSeconds == 0L) {
      sessionStartTime = null
      return
    }

    val averageVolume = volumeSpans.sumOf { it.volume.toLong() * it.durationMillis }.toFloat() / lengthMillis
    val spansCopy = volumeSpans.toList()

    scope.launch {
      val bookId = currentBookStore.data.first()
      if (bookId != null) {
        val session = PlaybackSession(
          bookId = bookId,
          startedAt = start,
          lengthInSeconds = lengthInSeconds,
          averageVolume = averageVolume,
          volumeSpans = spansCopy
        )
        sessionRepo.insert(session)
      }
    }
    sessionStartTime = null
  }
}
