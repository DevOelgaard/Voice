package voice.features.playbackScreen.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import voice.core.data.BookId
import voice.core.ui.sharedCoverElementModifier
import voice.core.strings.R as StringsR
import voice.core.ui.R as UiR

@Composable
internal fun Cover(
  bookId: BookId,
  onDoubleClick: () -> Unit,
  cover: String?,
  playing: Boolean,
) {
  val scale by animateFloatAsState(
    targetValue = if (playing) 1.0f else 0.9f,
    animationSpec = tween(durationMillis = 300),
    label = "coverScale",
  )
  AsyncImage(
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .sharedCoverElementModifier(bookId)
      .pointerInput(Unit) {
        detectTapGestures(
          onDoubleTap = {
            onDoubleClick()
          },
        )
      }
      .clip(RoundedCornerShape(20.dp)),
    contentScale = ContentScale.Crop,
    model = cover,
    placeholder = painterResource(id = UiR.drawable.album_art),
    error = painterResource(id = UiR.drawable.album_art),
    contentDescription = stringResource(id = StringsR.string.cover_title),
  )
}
