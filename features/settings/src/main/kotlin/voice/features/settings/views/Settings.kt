package voice.features.settings.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.core.ui.VoiceTheme
import voice.core.ui.icons.VoiceIcons
import voice.features.settings.SettingsListener
import voice.features.settings.SettingsViewEffect
import voice.features.settings.SettingsViewModel
import voice.features.settings.SettingsViewState
import voice.features.settings.views.sleeptimer.AutoSleepTimerCard
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.core.strings.R as StringsR

@Composable
@Preview
private fun SettingsPreview() {
  VoiceTheme {
    Settings(
      SettingsViewState.preview(),
      SettingsListener.noop(),
    )
  }
}

@Composable
private fun Settings(
  viewState: SettingsViewState,
  listener: SettingsListener,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

  var importUriToConfirm by remember { mutableStateOf<android.net.Uri?>(null) }
  val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
    if (uri != null) {
      listener.exportData(uri)
    }
  }
  val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    if (uri != null) {
      importUriToConfirm = uri
    }
  }

  if (importUriToConfirm != null) {
    AlertDialog(
      onDismissRequest = { importUriToConfirm = null },
      title = { Text("Import Data") },
      text = { Text("Are you sure you want to overwrite your current data and settings with the imported file?") },
      confirmButton = {
        TextButton(onClick = {
          importUriToConfirm?.let { listener.importData(it) }
          importUriToConfirm = null
        }) {
          Text("Import")
        }
      },
      dismissButton = {
        TextButton(onClick = { importUriToConfirm = null }) {
          Text("Cancel")
        }
      }
    )
  }

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    },
    topBar = {
      TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
          Text(stringResource(StringsR.string.settings_action_open))
        },
        navigationIcon = {
          IconButton(
            onClick = {
              listener.close()
            },
          ) {
            Icon(
              imageVector = VoiceIcons.Close,
              contentDescription = stringResource(StringsR.string.common_action_close),
            )
          }
        },
      )
    },
  ) { contentPadding ->
    LazyColumn(contentPadding = contentPadding) {
      if (viewState.showDeveloperMenu && !viewState.kioskMode) {
        item {
          SettingsCard(title = stringResource(StringsR.string.settings_category_developer)) {
            DeveloperMenuItem(
              onClick = listener::openDeveloperMenu,
            )
          }
        }
      }
      item {
        SettingsCard(title = stringResource(StringsR.string.settings_category_library)) {
          ListItem(
            modifier = Modifier.clickable { listener.openFolderPicker() },
            leadingContent = {
              Icon(
                imageVector = VoiceIcons.Book,
                contentDescription = stringResource(StringsR.string.library_folders_title),
              )
            },
            headlineContent = {
              Text(stringResource(StringsR.string.library_folders_title))
            },
            supportingContent = {
              Text(stringResource(StringsR.string.settings_library_folders_summary))
            },
          )
          ListItem(
            modifier = Modifier.clickable { listener.toggleGrid() },
            leadingContent = {
              val icon = if (viewState.useGrid) {
                VoiceIcons.GridView
              } else {
                VoiceIcons.ViewList
              }
              Icon(
                imageVector = icon,
                contentDescription = stringResource(StringsR.string.settings_library_use_grid_title),
              )
            },
            headlineContent = { Text(stringResource(StringsR.string.settings_library_use_grid_title)) },
            trailingContent = {
              val switchIcon: @Composable (() -> Unit)? = if (viewState.useGrid) {
                {
                  Icon(
                    imageVector = VoiceIcons.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                  )
                }
              } else {
                null
              }
              Switch(
                checked = viewState.useGrid,
                onCheckedChange = {
                  listener.toggleGrid()
                },
                thumbContent = switchIcon,
              )
            },
          )
          ListItem(
            modifier = Modifier.clickable { listener.toggleGroupByAuthor() },
            leadingContent = {
              Icon(
                imageVector = VoiceIcons.Person,
                contentDescription = stringResource(StringsR.string.settings_library_group_by_author_title),
              )
            },
            headlineContent = { Text(stringResource(StringsR.string.settings_library_group_by_author_title)) },
            trailingContent = {
              val switchIcon: @Composable (() -> Unit)? = if (viewState.groupByAuthor) {
                {
                  Icon(
                    imageVector = VoiceIcons.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                  )
                }
              } else {
                null
              }
              Switch(
                checked = viewState.groupByAuthor,
                onCheckedChange = {
                  listener.toggleGroupByAuthor()
                },
                thumbContent = switchIcon,
              )
            },
          )
        }
      }

      item {
        SettingsCard(title = stringResource(StringsR.string.settings_category_appearance)) {
          ThemeModeRow(viewState.themeMode, listener::onThemeModeRowClick)
          if (viewState.showThemeColorSchemePref) {
            ThemeColorSchemeRow(viewState.themeColorScheme, listener::onThemeColorSchemeRowClick)
          }
        }
      }

      if (viewState.showAnalyticSetting && !viewState.kioskMode) {
        item {
          SettingsCard(title = stringResource(StringsR.string.settings_category_privacy)) {
            AnalyticsRow(analyticsEnabled = viewState.analyticsEnabled, toggle = listener::toggleAnalytics)
          }
        }
      }

      item {
        SettingsCard(title = stringResource(StringsR.string.settings_category_playback)) {
          SeekTimeRow(viewState.seekTimeInSeconds) {
            listener.onSeekAmountRowClick()
          }
          AutoRewindRow(viewState.autoRewindInSeconds) {
            listener.onAutoRewindRowClick()
          }
          ListItem(
            modifier = Modifier.clickable { listener.toggleLockscreenSeeking() },
            leadingContent = {
              Icon(
                imageVector = VoiceIcons.LockOpen,
                contentDescription = stringResource(StringsR.string.settings_playback_lockscreen_seeking_title),
              )
            },
            headlineContent = { Text(stringResource(StringsR.string.settings_playback_lockscreen_seeking_title)) },
            supportingContent = { Text(stringResource(StringsR.string.settings_playback_lockscreen_seeking_summary)) },
            trailingContent = {
              val switchIcon: @Composable (() -> Unit)? = if (viewState.lockscreenSeekingEnabled) {
                {
                  Icon(
                    imageVector = VoiceIcons.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                  )
                }
              } else {
                null
              }
              Switch(
                checked = viewState.lockscreenSeekingEnabled,
                onCheckedChange = { listener.toggleLockscreenSeeking() },
                thumbContent = switchIcon,
              )
            },
          )
          ListItem(
            modifier = Modifier.clickable { listener.toggleAdjustTimeForPlaybackSpeed() },
            leadingContent = {
              Icon(
                imageVector = VoiceIcons.Speed,
                contentDescription = stringResource(StringsR.string.settings_playback_adjust_time_for_speed_title),
              )
            },
            headlineContent = { Text(stringResource(StringsR.string.settings_playback_adjust_time_for_speed_title)) },
            supportingContent = { Text(stringResource(StringsR.string.settings_playback_adjust_time_for_speed_summary)) },
            trailingContent = {
              val switchIcon: @Composable (() -> Unit)? = if (viewState.adjustTimeForPlaybackSpeed) {
                {
                  Icon(
                    imageVector = VoiceIcons.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                  )
                }
              } else {
                null
              }
              Switch(
                checked = viewState.adjustTimeForPlaybackSpeed,
                onCheckedChange = { listener.toggleAdjustTimeForPlaybackSpeed() },
                thumbContent = switchIcon,
              )
            },
          )
        }
      }

      item {
        AutoSleepTimerCard(viewState.autoSleepTimer, listener)
      }

      item {
        SettingsCard(title = stringResource(StringsR.string.settings_category_backup)) {
          ListItem(
            modifier = Modifier.clickable { exportLauncher.launch("voice_backup.json") },
            leadingContent = {
              Icon(
                imageVector = VoiceIcons.Download,
                contentDescription = "Export Backup",
              )
            },
            headlineContent = {
              Text("Export Backup")
            },
            supportingContent = {
              Text("Export progress and settings to a JSON file")
            },
          )
          ListItem(
            modifier = Modifier.clickable { importLauncher.launch(arrayOf("application/json", "*/*")) },
            leadingContent = {
              Icon(
                imageVector = VoiceIcons.Folder,
                contentDescription = "Import Backup",
              )
            },
            headlineContent = {
              Text("Import Backup")
            },
            supportingContent = {
              Text("Import progress and settings from a JSON file")
            },
          )
        }
      }

      item {
        SettingsCard(title = stringResource(StringsR.string.settings_category_support)) {
          if (viewState.showSupportDevelopment) {
            ListItem(
              modifier = Modifier.clickable { listener.openSupportVoice() },
              leadingContent = {
                Icon(
                  imageVector = VoiceIcons.Favorite,
                  contentDescription = stringResource(StringsR.string.settings_support_support_voice_title),
                  tint = MaterialTheme.colorScheme.primary,
                )
              },
              headlineContent = {
                Text(stringResource(StringsR.string.settings_support_support_voice_title))
              },
              supportingContent = {
                Text(stringResource(StringsR.string.settings_support_support_voice_summary))
              },
            )
          }

          ListItem(
            modifier = Modifier.clickable { listener.suggestIdea() },
            leadingContent = {
              Icon(
                imageVector = VoiceIcons.Lightbulb,
                contentDescription = stringResource(StringsR.string.settings_support_suggest_idea_title),
              )
            },
            headlineContent = {
              Text(stringResource(StringsR.string.settings_support_suggest_idea_title))
            },
          )

          ListItem(
            modifier = Modifier.clickable { listener.getSupport() },
            leadingContent = {
              Icon(
                imageVector = VoiceIcons.Help,
                contentDescription = stringResource(StringsR.string.settings_support_get_support_title),
              )
            },
            headlineContent = {
              Text(stringResource(StringsR.string.settings_support_get_support_title))
            },
          )

          ListItem(
            modifier = Modifier.clickable { listener.openBugReport() },
            leadingContent = {
              Icon(
                imageVector = VoiceIcons.BugReport,
                contentDescription = stringResource(StringsR.string.settings_support_report_issue_title),
              )
            },
            headlineContent = {
              Text(stringResource(StringsR.string.settings_support_report_issue_title))
            },
          )

          ListItem(
            modifier = Modifier.clickable { listener.openTranslations() },
            leadingContent = {
              Icon(
                imageVector = VoiceIcons.Language,
                contentDescription = stringResource(StringsR.string.settings_support_help_translating_title),
              )
            },
            headlineContent = {
              Text(stringResource(StringsR.string.settings_support_help_translating_title))
            },
          )

          ListItem(
            modifier = Modifier.clickable { listener.openFaq() },
            leadingContent = {
              Icon(
                imageVector = VoiceIcons.Help,
                contentDescription = stringResource(StringsR.string.settings_support_faq_title),
              )
            },
            headlineContent = {
              Text(stringResource(StringsR.string.settings_support_faq_title))
            },
          )
        }
      }

      item {
        SettingsCard(title = stringResource(StringsR.string.settings_category_about)) {
          AppVersion(
            appVersion = viewState.appVersion,
            onClick = listener::onAppVersionClick,
          )
        }
      }

      if (viewState.kioskMode) {
        if (viewState.showAnalyticSetting) {
          item {
            SettingsCard(title = stringResource(StringsR.string.settings_category_privacy)) {
              AnalyticsRow(analyticsEnabled = viewState.analyticsEnabled, toggle = listener::toggleAnalytics)
            }
          }
        }
        if (viewState.showDeveloperMenu) {
          item {
            SettingsCard(title = stringResource(StringsR.string.settings_category_developer)) {
              DeveloperMenuItem(
                onClick = listener::openDeveloperMenu,
              )
            }
          }
        }
      }
    }
    Dialog(viewState, listener)
  }
}

@Composable
private fun SettingsCard(
  modifier: Modifier = Modifier,
  title: String? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  OutlinedCard(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
  ) {
    Column(
      modifier = Modifier.padding(vertical = 8.dp),
    ) {
      if (title != null) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
      }
      content()
    }
  }
}

@Composable
private fun AnalyticsRow(
  analyticsEnabled: Boolean,
  toggle: () -> Unit,
) {
  ListItem(
    modifier = Modifier.clickable { toggle() },
    leadingContent = {
      Icon(
        imageVector = VoiceIcons.Analytics,
        contentDescription = null,
      )
    },
    headlineContent = {
      Text(text = stringResource(StringsR.string.settings_analytics_consent_title))
    },
    supportingContent = {
      Text(text = stringResource(StringsR.string.settings_analytics_consent_description))
    },
    trailingContent = {
      val switchIcon: @Composable (() -> Unit)? = if (analyticsEnabled) {
        {
          Icon(
            imageVector = VoiceIcons.Check,
            contentDescription = null,
            modifier = Modifier.size(SwitchDefaults.IconSize),
          )
        }
      } else {
        null
      }
      Switch(
        checked = analyticsEnabled,
        onCheckedChange = { toggle() },
        thumbContent = switchIcon,
      )
    },
  )
}

@ContributesTo(AppScope::class)
interface SettingsGraph {
  val settingsViewModel: SettingsViewModel
}

@ContributesTo(AppScope::class)
interface SettingsProvider {

  @Provides
  @IntoSet
  fun settingsNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Settings> { key ->
    NavEntry(key) {
      Settings()
    }
  }
}

@Composable
fun Settings() {
  val viewModel = retain<SettingsViewModel> { rootGraphAs<SettingsGraph>().settingsViewModel }
  val snackbarHostState = remember { SnackbarHostState() }
  val viewState = viewModel.viewState()
  val currentDeveloperMenuUnlockedMessage = rememberUpdatedState("Developer Menu unlocked")
  LaunchedEffect(viewModel) {
    viewModel.viewEffects.collect { viewEffect ->
      when (viewEffect) {
        SettingsViewEffect.DeveloperMenuUnlocked -> {
          snackbarHostState.showSnackbar(currentDeveloperMenuUnlockedMessage.value)
        }
      }
    }
  }
  Settings(viewState, viewModel, snackbarHostState)
}

@Composable
private fun Dialog(
  viewState: SettingsViewState,
  listener: SettingsListener,
) {
  val dialog = viewState.dialog ?: return
  when (dialog) {
    SettingsViewState.Dialog.AutoRewindAmount -> {
      AutoRewindAmountDialog(
        currentSeconds = viewState.autoRewindInSeconds,
        onSecondsConfirm = listener::autoRewindAmountChang,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.SeekTime -> {
      SeekAmountDialog(
        currentSeconds = viewState.seekTimeInSeconds,
        onSecondsConfirm = listener::seekAmountChanged,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.Theme -> {
      ThemeModeDialog(
        selectedThemeMode = viewState.themeMode,
        onThemeModeSelect = listener::setThemeMode,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.ColorScheme -> {
      ThemeColorSchemeDialog(
        selectedThemeColorScheme = viewState.themeColorScheme,
        onThemeColorSchemeSelect = listener::setThemeColorScheme,
        onDismiss = listener::dismissDialog,
      )
    }
  }
}
