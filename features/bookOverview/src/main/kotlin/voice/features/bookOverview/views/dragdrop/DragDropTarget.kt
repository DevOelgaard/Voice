package voice.features.bookOverview.views.dragdrop

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import voice.core.data.BookId

// Information about a potential drop target
sealed class DropTargetInfo {
  data class SeriesHeader(val seriesName: String) : DropTargetInfo()
  data class Book(val bookId: BookId, val seriesName: String?, val seriesPart: String?) : DropTargetInfo()
}

// Global registry of all drop targets currently on screen
object DropTargetRegistry {
  private val targets = mutableMapOf<DropTargetInfo, Rect>()

  fun registerTarget(info: DropTargetInfo, bounds: Rect) {
    targets[info] = bounds
  }

  fun unregisterTarget(info: DropTargetInfo) {
    targets.remove(info)
  }

  // Find which target the given position is hovering over
  fun findTarget(position: Offset): Pair<DropTargetInfo, Rect>? {
    for ((info, bounds) in targets) {
      if (bounds.contains(position)) {
        return Pair(info, bounds)
      }
    }
    return null
  }
}

fun Modifier.dragDropTarget(info: DropTargetInfo): Modifier = composed {
  var isRegistered by remember { mutableStateOf(false) }
  
  DisposableEffect(info) {
    onDispose {
      if (isRegistered) {
        DropTargetRegistry.unregisterTarget(info)
        isRegistered = false
      }
    }
  }

  this.onGloballyPositioned { coordinates ->
    DropTargetRegistry.registerTarget(info, coordinates.boundsInWindow())
    isRegistered = true
  }
}
