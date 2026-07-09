package voice.core.data.repo.internals

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.room.withTransaction
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import voice.core.data.BookContent
import voice.core.data.BookId
import voice.core.data.Bookmark
import voice.core.data.Chapter
import voice.core.data.ChapterId
import voice.core.data.GridMode
import voice.core.data.RecentBookSearch
import voice.core.data.ThemeColorScheme
import voice.core.data.ThemeMode
import voice.core.data.backup.BackupData
import voice.core.data.backup.BackupRepository
import voice.core.data.backup.BookContentBackup
import voice.core.data.backup.BookmarkBackup
import voice.core.data.backup.ChapterBackup
import voice.core.data.backup.SettingsBackup
import voice.core.data.folders.AuthorAudiobookFoldersStore
import voice.core.data.folders.RootAudiobookFoldersStore
import voice.core.data.folders.SingleFileAudiobookFoldersStore
import voice.core.data.folders.SingleFolderAudiobookFoldersStore
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.data.store.AmountOfBatteryOptimizationRequestedStore
import voice.core.data.store.AnalyticsConsentStore
import voice.core.data.store.AutoRewindAmountStore
import voice.core.data.store.CurrentBookStore
import voice.core.data.store.DeveloperMenuUnlockedStore
import voice.core.data.store.ExpandedAuthorsStore
import voice.core.data.store.FadeOutStore
import voice.core.data.store.FolderPickerMovedDialogShownStore
import voice.core.data.store.GridModeStore
import voice.core.data.store.GroupByAuthorStore
import voice.core.data.store.LockscreenSeekingEnabledStore
import voice.core.data.store.OnboardingCompletedStore
import voice.core.data.store.ReviewDialogShownStore
import voice.core.data.store.SeekTimeStore
import voice.core.data.store.SleepTimerPreferenceStore
import voice.core.data.store.ThemeColorSchemeStore
import voice.core.data.store.ThemeModeStore
import java.io.File
import java.time.Instant
import dev.zacsweers.metro.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration
import kotlin.uuid.Uuid

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
public class BackupRepositoryImpl(
  private val appDb: AppDb,
  @ThemeModeStore private val themeModeStore: DataStore<ThemeMode>,
  @ThemeColorSchemeStore private val themeColorSchemeStore: DataStore<ThemeColorScheme>,
  @AutoRewindAmountStore private val autoRewindAmountStore: DataStore<Int>,
  @FadeOutStore private val fadeOutStore: DataStore<Duration>,
  @SeekTimeStore private val seekTimeStore: DataStore<Int>,
  @SleepTimerPreferenceStore private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  @GridModeStore private val gridModeStore: DataStore<GridMode>,
  @OnboardingCompletedStore private val onboardingCompletedStore: DataStore<Boolean>,
  @CurrentBookStore private val currentBookStore: DataStore<BookId?>,
  @AmountOfBatteryOptimizationRequestedStore private val batteryOptimizationsStore: DataStore<Int>,
  @ReviewDialogShownStore private val reviewDialogShownStore: DataStore<Boolean>,
  @FolderPickerMovedDialogShownStore private val folderPickerMovedDialogShownStore: DataStore<Boolean>,
  @AnalyticsConsentStore private val analyticsConsentStore: DataStore<Boolean>,
  @DeveloperMenuUnlockedStore private val developerMenuUnlockedStore: DataStore<Boolean>,
  @LockscreenSeekingEnabledStore private val lockscreenSeekingEnabledStore: DataStore<Boolean>,
  @GroupByAuthorStore private val groupByAuthorStore: DataStore<Boolean>,
  @ExpandedAuthorsStore private val expandedAuthorsStore: DataStore<Set<String>>,
  @RootAudiobookFoldersStore private val rootAudiobookFoldersStore: DataStore<Set<Uri>>,
  @SingleFolderAudiobookFoldersStore private val singleFolderAudiobookFoldersStore: DataStore<Set<Uri>>,
  @SingleFileAudiobookFoldersStore private val singleFileAudiobookFoldersStore: DataStore<Set<Uri>>,
  @AuthorAudiobookFoldersStore private val authorAudiobookFoldersStore: DataStore<Set<Uri>>,
) : BackupRepository {

  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = false
  }

  override suspend fun exportData(): String {
    val bookContents = appDb.bookContentDao().all().map { it.toBackup() }
    val chapters = appDb.chapterDao().all().map { it.toBackup() }
    val bookmarks = appDb.bookmarkDao().all().map { it.toBackup() }
    val recentSearches = appDb.recentBookSearchDao().recentBookSearch()

    val settings = SettingsBackup(
      themeMode = themeModeStore.data.first(),
      themeColorScheme = themeColorSchemeStore.data.first(),
      autoRewindAmount = autoRewindAmountStore.data.first(),
      fadeOutDurationMs = fadeOutStore.data.first().inWholeMilliseconds,
      seekTime = seekTimeStore.data.first(),
      sleepTimerPreference = sleepTimerPreferenceStore.data.first(),
      gridMode = gridModeStore.data.first(),
      onboardingCompleted = onboardingCompletedStore.data.first(),
      currentBook = currentBookStore.data.first(),
      amountOfBatteryOptimizationsRequested = batteryOptimizationsStore.data.first(),
      reviewDialogShown = reviewDialogShownStore.data.first(),
      folderPickerMovedDialogShown = folderPickerMovedDialogShownStore.data.first(),
      analyticsConsent = analyticsConsentStore.data.first(),
      developerMenuUnlocked = developerMenuUnlockedStore.data.first(),
      lockscreenSeekingEnabled = lockscreenSeekingEnabledStore.data.first(),
      groupByAuthor = groupByAuthorStore.data.first(),
      expandedAuthors = expandedAuthorsStore.data.first(),
      rootAudioBookFolders = rootAudiobookFoldersStore.data.first().map { it.toString() }.toSet(),
      singleFolderAudiobookFolders = singleFolderAudiobookFoldersStore.data.first().map { it.toString() }.toSet(),
      singleFileAudiobookFolders = singleFileAudiobookFoldersStore.data.first().map { it.toString() }.toSet(),
      authorAudiobookFolders = authorAudiobookFoldersStore.data.first().map { it.toString() }.toSet(),
    )

    val backupData = BackupData(
      bookContents = bookContents,
      chapters = chapters,
      bookmarks = bookmarks,
      recentSearches = recentSearches,
      settings = settings,
    )

    return json.encodeToString(backupData)
  }

  override suspend fun importData(jsonString: String) {
    val backupData = json.decodeFromString<BackupData>(jsonString)

    appDb.withTransaction {
      // Clear current data or replace? 
      // The open question was resolved: we prompt user before this. So here we just replace.
      // Room's Insert with OnConflictStrategy.REPLACE is used in DAOs usually, but we need to check if we can insert all.
      // Wait, there might be foreign keys or other constraints. Let's insert them.
      // To be safe, we could delete all existing? If the user agreed to overwrite, yes.
      appDb.bookContentDao().deleteAll()
      appDb.chapterDao().deleteAll()
      appDb.bookmarkDao().deleteAll()
      appDb.recentBookSearchDao().deleteAll()

      backupData.chapters.map { it.toEntity() }.forEach { appDb.chapterDao().insert(it) }
      backupData.bookContents.map { it.toEntity() }.forEach { appDb.bookContentDao().insert(it) }
      backupData.bookmarks.map { it.toEntity() }.forEach { appDb.bookmarkDao().addBookmark(it) }
      backupData.recentSearches.forEach { appDb.recentBookSearchDao().addRaw(it) }
    }

    // Update settings
    backupData.settings.themeMode?.let { v -> themeModeStore.updateData { v } }
    backupData.settings.themeColorScheme?.let { v -> themeColorSchemeStore.updateData { v } }
    backupData.settings.autoRewindAmount?.let { v -> autoRewindAmountStore.updateData { v } }
    backupData.settings.fadeOutDurationMs?.let { v -> fadeOutStore.updateData { v.milliseconds } }
    backupData.settings.seekTime?.let { v -> seekTimeStore.updateData { v } }
    backupData.settings.sleepTimerPreference?.let { v -> sleepTimerPreferenceStore.updateData { v } }
    backupData.settings.gridMode?.let { v -> gridModeStore.updateData { v } }
    backupData.settings.onboardingCompleted?.let { v -> onboardingCompletedStore.updateData { v } }
    backupData.settings.currentBook?.let { v -> currentBookStore.updateData { v } }
    backupData.settings.amountOfBatteryOptimizationsRequested?.let { v -> batteryOptimizationsStore.updateData { v } }
    backupData.settings.reviewDialogShown?.let { v -> reviewDialogShownStore.updateData { v } }
    backupData.settings.folderPickerMovedDialogShown?.let { v -> folderPickerMovedDialogShownStore.updateData { v } }
    backupData.settings.analyticsConsent?.let { v -> analyticsConsentStore.updateData { v } }
    backupData.settings.developerMenuUnlocked?.let { v -> developerMenuUnlockedStore.updateData { v } }
    backupData.settings.lockscreenSeekingEnabled?.let { v -> lockscreenSeekingEnabledStore.updateData { v } }
    backupData.settings.groupByAuthor?.let { v -> groupByAuthorStore.updateData { v } }
    backupData.settings.expandedAuthors?.let { v -> expandedAuthorsStore.updateData { v } }
    backupData.settings.rootAudioBookFolders?.let { v -> rootAudiobookFoldersStore.updateData { v.map { Uri.parse(it) }.toSet() } }
    backupData.settings.singleFolderAudiobookFolders?.let { v -> singleFolderAudiobookFoldersStore.updateData { v.map { Uri.parse(it) }.toSet() } }
    backupData.settings.singleFileAudiobookFolders?.let { v -> singleFileAudiobookFoldersStore.updateData { v.map { Uri.parse(it) }.toSet() } }
    backupData.settings.authorAudiobookFolders?.let { v -> authorAudiobookFoldersStore.updateData { v.map { Uri.parse(it) }.toSet() } }
  }

  private fun BookContent.toBackup() = BookContentBackup(
    id = id,
    playbackSpeed = playbackSpeed,
    skipSilence = skipSilence,
    isActive = isActive,
    lastPlayedAtEpochMilli = lastPlayedAt.toEpochMilli(),
    author = author,
    name = name,
    addedAtEpochMilli = addedAt.toEpochMilli(),
    chapters = chapters,
    currentChapter = currentChapter,
    positionInChapter = positionInChapter,
    coverPath = cover?.absolutePath,
    gain = gain,
    genre = genre,
    narrator = narrator,
    series = series,
    part = part,
  )

  private fun BookContentBackup.toEntity() = BookContent(
    id = id,
    playbackSpeed = playbackSpeed,
    skipSilence = skipSilence,
    isActive = isActive,
    lastPlayedAt = Instant.ofEpochMilli(lastPlayedAtEpochMilli),
    author = author,
    name = name,
    addedAt = Instant.ofEpochMilli(addedAtEpochMilli),
    chapters = chapters,
    currentChapter = currentChapter,
    positionInChapter = positionInChapter,
    cover = coverPath?.let { File(it) },
    gain = gain,
    genre = genre,
    narrator = narrator,
    series = series,
    part = part,
  )

  private fun Chapter.toBackup() = ChapterBackup(
    id = id,
    name = name,
    duration = duration,
    fileLastModifiedEpochMilli = fileLastModified.toEpochMilli(),
    fileSize = fileSize,
    markData = markData,
  )

  private fun ChapterBackup.toEntity() = Chapter(
    id = id,
    name = name,
    duration = duration,
    fileLastModified = Instant.ofEpochMilli(fileLastModifiedEpochMilli),
    fileSize = fileSize,
    markData = markData,
  )

  private fun Bookmark.toBackup() = BookmarkBackup(
    bookId = bookId,
    chapterId = chapterId,
    title = title,
    time = time,
    addedAtEpochMilli = addedAt.toEpochMilli(),
    setBySleepTimer = setBySleepTimer,
    id = id.value.toString(),
  )

  private fun BookmarkBackup.toEntity() = Bookmark(
    bookId = bookId,
    chapterId = chapterId,
    title = title,
    time = time,
    addedAt = Instant.ofEpochMilli(addedAtEpochMilli),
    setBySleepTimer = setBySleepTimer,
    id = Bookmark.Id(Uuid.parse(id)),
  )
}
