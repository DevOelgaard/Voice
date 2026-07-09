package voice.core.data.backup

import kotlinx.serialization.Serializable
import voice.core.data.BookId
import voice.core.data.ChapterId
import voice.core.data.GridMode
import voice.core.data.MarkData
import voice.core.data.ThemeColorScheme
import voice.core.data.ThemeMode
import voice.core.data.sleeptimer.SleepTimerPreference

@Serializable
public data class BackupData(
  val version: Int = 1,
  val bookContents: List<BookContentBackup> = emptyList(),
  val chapters: List<ChapterBackup> = emptyList(),
  val bookmarks: List<BookmarkBackup> = emptyList(),
  val recentSearches: List<String> = emptyList(),
  val settings: SettingsBackup = SettingsBackup(),
)

@Serializable
public data class BookContentBackup(
  val id: BookId,
  val playbackSpeed: Float,
  val skipSilence: Boolean,
  val isActive: Boolean,
  val lastPlayedAtEpochMilli: Long,
  val author: String?,
  val name: String,
  val addedAtEpochMilli: Long,
  val chapters: List<ChapterId>,
  val currentChapter: ChapterId,
  val positionInChapter: Long,
  val coverPath: String?,
  val gain: Float,
  val genre: String?,
  val narrator: String?,
  val series: String?,
  val part: String?,
)

@Serializable
public data class ChapterBackup(
  val id: ChapterId,
  val name: String?,
  val duration: Long,
  val fileLastModifiedEpochMilli: Long,
  val fileSize: Long,
  val markData: List<MarkData>,
)

@Serializable
public data class BookmarkBackup(
  val bookId: BookId,
  val chapterId: ChapterId,
  val title: String?,
  val time: Long,
  val addedAtEpochMilli: Long,
  val setBySleepTimer: Boolean,
  val id: String,
)

@Serializable
public data class SettingsBackup(
  val themeMode: ThemeMode? = null,
  val themeColorScheme: ThemeColorScheme? = null,
  val autoRewindAmount: Int? = null,
  val fadeOutDurationMs: Long? = null,
  val seekTime: Int? = null,
  val sleepTimerPreference: SleepTimerPreference? = null,
  val gridMode: GridMode? = null,
  val onboardingCompleted: Boolean? = null,
  val currentBook: BookId? = null,
  val amountOfBatteryOptimizationsRequested: Int? = null,
  val reviewDialogShown: Boolean? = null,
  val folderPickerMovedDialogShown: Boolean? = null,
  val analyticsConsent: Boolean? = null,
  val developerMenuUnlocked: Boolean? = null,
  val lockscreenSeekingEnabled: Boolean? = null,
  val groupByAuthor: Boolean? = null,
  val expandedAuthors: Set<String>? = null,
  val rootAudioBookFolders: Set<String>? = null,
  val singleFolderAudiobookFolders: Set<String>? = null,
  val singleFileAudiobookFolders: Set<String>? = null,
  val authorAudiobookFolders: Set<String>? = null,
)
