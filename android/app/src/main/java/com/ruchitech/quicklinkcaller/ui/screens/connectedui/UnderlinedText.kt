package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap.Companion.Square
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun UnderlinedText(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    onDone: () -> Unit
) {
    var textWidth by remember { mutableStateOf(0f) }
    var textHeight by remember { mutableStateOf(0f) }
    var textValue by remember { mutableStateOf(text) }

    Row(modifier = modifier.wrapContentWidth()) {
        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned {
                    textWidth = it.size.width.toFloat()
                    textHeight = it.size.height.toFloat()
                },
            value = textValue,
            onValueChange = { newText ->
                textValue = newText
                onTextChange(newText)
            },
            textStyle = MaterialTheme.typography.bodyMedium
        )

        Box(
            modifier = Modifier
                .size(width = textWidth.dp, height = textHeight.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, textHeight),
                    end = Offset(textWidth, textHeight),
                    strokeWidth = 2f,
                    cap = Square
                )
            }
        }
    }
}

@Composable
fun HintedTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    onDone: () -> Unit,
    focusRequester: FocusRequester,
    onDelete: () -> Unit,
    onChecked: Boolean
) {
    var isHintVisible by remember { mutableStateOf(value.isEmpty()) }
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = value ?: "",
                selection = TextRange(value?.length ?: 0)
            )
        )
    }
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
/*
        TextField(
            value = value, onValueChange = { onValueChange(it) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onDone()
            }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = Color.Black,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            placeholder = {
                Text(text = hint)
            }
        )*/

        BasicTextField(
            modifier = Modifier
                .padding(horizontal = 0.dp)
                .padding(bottom = if (isHintVisible) 0.dp else 0.dp)
                .focusRequester(focusRequester)
                .onKeyEvent { keyEvent ->
                    Log.e("kjuhyg", "HintedTextField: ${keyEvent.type}")
                    if (keyEvent.type == KeyEventType.KeyUp
                    ) {
                        Log.e("kjuhyg", "HintedTextField: working")
                        if (value.isEmpty()) {
                            onDelete()
                        }
                    }
                    return@onKeyEvent false
                },
            value = textFieldValueState,
            onValueChange = {
                textFieldValueState = it
                onValueChange(it.text)
                isHintVisible = it.text.isEmpty()
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onDone()
            }),
            textStyle = TextStyle(textDecoration = if (onChecked) TextDecoration.LineThrough else TextDecoration.None)
        )

        if (isHintVisible) {
            Text(
                text = hint,
                color = Color.Gray,
                modifier = Modifier.padding(start = 0.dp, bottom = 0.dp)
            )
        }
    }


}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BasicTextFieldWithBackspaceDetection(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        modifier = Modifier
            .padding(horizontal = 0.dp)
            .padding(bottom = if (value.isEmpty()) 0.dp else 0.dp)
            .focusRequester(focusRequester),
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onDone()
            }
        ),
    )
}