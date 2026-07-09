package voice.features.bookOverview.views.dragdrop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import voice.core.data.BookId
import kotlin.math.roundToInt

@Composable
fun DragDropOverlay() {
  val state = LocalDragDropState.current ?: return
  if (!state.isDragging) return

  val draggedBook = state.draggedBook ?: return
  
  // Find hover target
  var hoverTarget by remember { mutableStateOf<Pair<DropTargetInfo, Rect>?>(null) }
  var isBeforeTarget by remember { mutableStateOf(false) }

  LaunchedEffect(state.dragPosition) {
    val target = DropTargetRegistry.findTarget(state.dragPosition)
    hoverTarget = target
    if (target != null) {
      val bounds = target.second
      // If dropping on a book, determine if it's top/left half or bottom/right half
      // We'll use simple Y coordinate for list and grid roughly
      isBeforeTarget = state.dragPosition.y < bounds.center.y
    }
  }

  // Draw drop indicator
  hoverTarget?.let { (info, bounds) ->
    val indicatorColor = MaterialTheme.colorScheme.primary
    val lineThickness = 4.dp
    val density = LocalDensity.current
    
    when (info) {
      is DropTargetInfo.SeriesHeader -> {
        // Draw a box around the header
        Box(
          modifier = Modifier
            .offset { IntOffset(bounds.left.roundToInt(), bounds.top.roundToInt()) }
            .width(with(density) { bounds.width.toDp() })
            .height(with(density) { bounds.height.toDp() })
            .background(indicatorColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        )
      }
      is DropTargetInfo.Book -> {
        // Draw a line above or below
        val yOffset = if (isBeforeTarget) bounds.top else bounds.bottom
        Box(
          modifier = Modifier
            .offset { IntOffset(bounds.left.roundToInt(), (yOffset - with(density) { (lineThickness/2).toPx() }).roundToInt()) }
            .width(with(density) { bounds.width.toDp() })
            .height(lineThickness)
            .background(indicatorColor, RoundedCornerShape(2.dp))
        )
      }
    }
  }

  // Draw ghost item
  Box(
    modifier = Modifier
      .offset {
        val x = (state.dragPosition.x - with(density) { 100.dp.toPx() }).roundToInt()
        val y = (state.dragPosition.y - with(density) { 40.dp.toPx() }).roundToInt()
        IntOffset(x, y)
      }
      .alpha(0.8f)
  ) {
    // We just draw a simple placeholder or card to represent the dragged book
    // since drawing the exact composable out-of-tree requires generic capture
    androidx.compose.material3.ElevatedCard(
      shape = MaterialTheme.shapes.extraLarge,
      modifier = Modifier.width(200.dp).height(80.dp)
    ) {
       androidx.compose.foundation.layout.Box(
           modifier = Modifier.fillMaxWidth().height(80.dp),
           contentAlignment = androidx.compose.ui.Alignment.Center
       ) {
           androidx.compose.material3.Text(text = draggedBook.name, modifier = Modifier.padding(16.dp), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
       }
    }
  }
  
  // Handle Drop is handled by DragDropState.onDragEnd directly now
}
