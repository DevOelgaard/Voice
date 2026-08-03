package voice.features.bookOverview.views.topbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import voice.features.bookOverview.views.BookFolderIcon
import voice.features.bookOverview.views.SettingsIcon
import voice.features.bookOverview.views.StatisticsIcon

@Composable
internal fun ColumnScope.TopBarTrailingIcon(
  searchActive: Boolean,
  showAddBookHint: Boolean,
  showFolderPickerIcon: Boolean,
  onBookFolderClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onStatisticsClick: () -> Unit,
) {
  AnimatedVisibility(
    visible = !searchActive,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    Row {
      StatisticsIcon(onStatisticsClick)
      if (showFolderPickerIcon) {
        BookFolderIcon(withHint = showAddBookHint, onClick = onBookFolderClick)
      }
      SettingsIcon(onSettingsClick)
    }
  }
}
