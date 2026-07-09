package voice.features.playbackScreen.view

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import voice.core.ui.formatTime
import kotlin.math.sin
import kotlin.time.Duration

@Composable
internal fun SliderRow(
  playing: Boolean,
  duration: Duration,
  playedTime: Duration,
  onSeek: (Duration) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    var localValue by remember { mutableFloatStateOf(0F) }
    val interactionSource = remember { MutableInteractionSource() }
    val dragging by interactionSource.collectIsDraggedAsState()
    Text(
      text = formatTime(
        timeMs = if (dragging) {
          (duration * localValue.toDouble()).inWholeMilliseconds
        } else {
          playedTime.inWholeMilliseconds
        },
        durationMs = duration.inWholeMilliseconds,
      ),
    )

    val phase by rememberInfiniteTransition(label = "wave").animateFloat(
      initialValue = 0f,
      targetValue = (2 * Math.PI).toFloat(),
      animationSpec = infiniteRepeatable(
        animation = tween(1500, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      ),
      label = "wavePhase"
    )

    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant

    Slider(
      modifier = Modifier
        .weight(1F)
        .padding(horizontal = 8.dp)
        .drawBehind {
          val width = size.width
          val height = size.height
          val centerY = height / 2f
          val sliderValue = if (dragging) {
            localValue
          } else {
            (playedTime / duration).toFloat().coerceIn(0F, 1F)
          }

          val thumbRadius = 10.dp.toPx()
          val trackStart = thumbRadius
          val trackEnd = width - thumbRadius
          val trackWidth = trackEnd - trackStart

          val activeWidth = trackStart + trackWidth * sliderValue

          // Draw Wavy Active Track
          val waveAmplitude = 3.dp.toPx()
          val waveFrequency = 0.1f

          val path = Path()
          path.moveTo(trackStart, centerY)
          
          var x = trackStart
          while (x <= activeWidth) {
            val currentPhase = if (playing) phase else 0f
            val y = centerY + sin(((x - trackStart) * waveFrequency) + currentPhase) * waveAmplitude
            if (x == trackStart) path.moveTo(x, y) else path.lineTo(x, y)
            x += 2f
          }
          // Ensure it connects cleanly to the thumb
          path.lineTo(activeWidth, centerY)

          drawPath(
            path = path,
            color = activeColor,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
          )

          // Draw Inactive Straight Track
          if (activeWidth < trackEnd) {
            drawLine(
              color = inactiveColor,
              start = Offset(activeWidth, centerY),
              end = Offset(trackEnd, centerY),
              strokeWidth = 4.dp.toPx(),
              cap = StrokeCap.Round
            )
          }
        },
      colors = SliderDefaults.colors(
        activeTrackColor = Color.Transparent,
        inactiveTrackColor = Color.Transparent
      ),
      interactionSource = interactionSource,
      value = if (dragging) {
        localValue
      } else {
        (playedTime / duration).toFloat()
          .coerceIn(0F, 1F)
      },
      onValueChange = {
        localValue = it
      },
      onValueChangeFinished = {
        onSeek(duration * localValue.toDouble())
      },
    )
    Text(
      text = formatTime(
        timeMs = duration.inWholeMilliseconds,
        durationMs = duration.inWholeMilliseconds,
      ),
    )
  }
}
