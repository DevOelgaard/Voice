package voice.features.bookOverview.views

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import voice.core.ui.icons.VoiceIcons

@Composable
internal fun StatisticsIcon(onStatisticsClick: () -> Unit) {
  IconButton(onStatisticsClick) {
    Icon(
      imageVector = VoiceIcons.Analytics,
      contentDescription = "Statistics",
    )
  }
}
