package voice.features.bookOverview.views.dragdrop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import voice.features.bookOverview.overview.BookOverviewItemViewState

class DragDropState(
  val onDropAction: (voice.core.data.BookId, DropTargetInfo, Boolean) -> Unit
) {
  var isDragging by mutableStateOf(false)
  var draggedBook by mutableStateOf<BookOverviewItemViewState?>(null)
  
  // The current absolute position of the pointer
  var dragPosition by mutableStateOf(Offset.Zero)
  
  // The offset relative to the top-left of the dragged item when drag started
  var dragOffset by mutableStateOf(Offset.Zero)

  fun onDragStart(book: BookOverviewItemViewState, pointerPosition: Offset, itemOffset: Offset) {
    draggedBook = book
    dragPosition = pointerPosition
    dragOffset = itemOffset
    isDragging = true
  }

  fun onDrag(dragAmount: Offset) {
    dragPosition += dragAmount
  }

  fun onDragEnd() {
    if (isDragging) {
      val target = DropTargetRegistry.findTarget(dragPosition)
      val isBeforeTarget = target?.let { dragPosition.y < it.second.center.y } ?: false
      if (target != null && draggedBook != null) {
        onDropAction(draggedBook!!.id, target.first, isBeforeTarget)
      }
    }
    isDragging = false
    draggedBook = null
    dragPosition = Offset.Zero
    dragOffset = Offset.Zero
  }
  
  fun onDragCancel() {
    onDragEnd()
  }
}

val LocalDragDropState = compositionLocalOf<DragDropState?> { null }

@Composable
fun ProvideDragDropState(
  onDropAction: (voice.core.data.BookId, DropTargetInfo, Boolean) -> Unit,
  content: @Composable () -> Unit
) {
  val state = remember(onDropAction) { DragDropState(onDropAction) }
  CompositionLocalProvider(LocalDragDropState provides state) {
    content()
  }
}
