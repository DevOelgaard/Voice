package voice.features.playbackScreen.view

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.strings.R
import voice.core.ui.icons.VoiceIcons

@Composable
internal fun SkipButton(
  forward: Boolean,
  onClick: () -> Unit,
) {
  FilledIconButton(
    modifier = Modifier.size(56.dp),
    onClick = onClick,
    shape = CircleShape,
    colors = IconButtonDefaults.filledIconButtonColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
  ) {
    Icon(
      modifier = Modifier.size(32.dp),
      imageVector = if (forward) VoiceIcons.FastForward else VoiceIcons.FastRewind,
      contentDescription = stringResource(
        id = if (forward) {
          R.string.playback_action_fast_forward
        } else {
          R.string.playback_action_rewind
        },
      ),
    )
  }
}
