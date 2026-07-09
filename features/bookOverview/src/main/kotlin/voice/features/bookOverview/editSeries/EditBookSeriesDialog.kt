package voice.features.bookOverview.editSeries

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import voice.core.strings.R as StringsR
import voice.core.ui.icons.VoiceIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditBookSeriesDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
  viewState: EditBookSeriesState,
  onUpdateSeries: (String) -> Unit,
  onUpdatePart: (String) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val seriesFocusRequester = remember { FocusRequester() }
  val partFocusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) {
    seriesFocusRequester.requestFocus()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(text = stringResource(StringsR.string.book_edit_series_title))
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
      ) {
        Text(stringResource(id = StringsR.string.common_dialog_confirm))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(id = StringsR.string.common_dialog_cancel))
      }
    },
    text = {
      Column {
        if (viewState.suggestedSeries.isNotEmpty()) {
          ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
          ) {
            OutlinedTextField(
              value = viewState.currentSeries,
              onValueChange = {
                  expanded = true
                  onUpdateSeries(it)
              },
              label = {
                Text(stringResource(StringsR.string.book_edit_series_label))
              },
              trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
              },
              colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
              keyboardActions = KeyboardActions(onNext = { partFocusRequester.requestFocus() }),
              singleLine = true,
              modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true)
                .fillMaxWidth()
                .focusRequester(seriesFocusRequester),
            )
            ExposedDropdownMenu(
              expanded = expanded,
              onDismissRequest = { expanded = false },
            ) {
              val filteredOptions = viewState.suggestedSeries.filter {
                  it.contains(viewState.currentSeries, ignoreCase = true)
              }
              filteredOptions.forEach { selectionOption ->
                DropdownMenuItem(
                  text = { Text(selectionOption) },
                  onClick = {
                    onUpdateSeries(selectionOption)
                    expanded = false
                  }
                )
              }
            }
          }
        } else {
          OutlinedTextField(
            value = viewState.currentSeries,
            onValueChange = onUpdateSeries,
            label = {
              Text(stringResource(StringsR.string.book_edit_series_label))
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { partFocusRequester.requestFocus() }),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(seriesFocusRequester),
          )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
          value = viewState.currentPart,
          onValueChange = onUpdatePart,
          label = {
            Text(stringResource(StringsR.string.book_edit_series_part_label))
          },
          trailingIcon = {
            if (viewState.currentPart.isNotEmpty()) {
              IconButton(onClick = { onUpdatePart("") }) {
                Icon(
                  imageVector = VoiceIcons.Close,
                  contentDescription = stringResource(StringsR.string.common_action_clear)
                )
              }
            }
          },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = { onConfirm() }
          ),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(partFocusRequester),
        )
      }
    },
  )
}
