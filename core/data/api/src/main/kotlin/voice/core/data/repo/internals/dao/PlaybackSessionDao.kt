package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import voice.core.data.PlaybackSession

@Dao
public interface PlaybackSessionDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(session: PlaybackSession)

  @Query("SELECT * FROM playbackSessions ORDER BY startedAt DESC")
  public suspend fun getAll(): List<PlaybackSession>

  @Query("SELECT * FROM playbackSessions ORDER BY startedAt DESC")
  public fun flowAll(): kotlinx.coroutines.flow.Flow<List<PlaybackSession>>

  @Query("SELECT * FROM playbackSessions WHERE bookId = :bookId ORDER BY startedAt DESC")
  public suspend fun getForBook(bookId: String): List<PlaybackSession>

  @Query("DELETE FROM playbackSessions")
  public suspend fun deleteAll()
}
