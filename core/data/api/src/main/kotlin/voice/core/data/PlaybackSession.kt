package voice.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
public data class VolumeSpan(
  val volume: Int,
  val durationMillis: Long,
)

@Entity(tableName = "playbackSessions")
public data class PlaybackSession(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val bookId: BookId,
  val startedAt: Instant,
  val lengthInSeconds: Long,
  val averageVolume: Float,
  val volumeSpans: List<VolumeSpan>,
)
