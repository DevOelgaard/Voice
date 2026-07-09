package voice.features.bookOverview.views.dragdrop

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import voice.features.bookOverview.overview.BookOverviewItemViewState

fun Modifier.dragDropSource(
  book: BookOverviewItemViewState,
  dragDropState: DragDropState,
): Modifier {
  var globalPosition = Offset.Zero
  return this
    .onGloballyPositioned { coordinates ->
      globalPosition = coordinates.positionInWindow()
    }
    .pointerInput(Unit) {
      detectDragGestures(
        onDragStart = { offset ->
          val dragStartGlobal = globalPosition + offset
          dragDropState.onDragStart(book, dragStartGlobal, offset)
        },
        onDrag = { change, dragAmount ->
          change.consume()
          dragDropState.onDrag(dragAmount)
        },
        onDragEnd = {
          dragDropState.onDragEnd()
        },
        onDragCancel = {
          dragDropState.onDragCancel()
        }
      )
    }
}
