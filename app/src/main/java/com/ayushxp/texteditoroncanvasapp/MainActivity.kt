package com.ayushxp.texteditoroncanvasapp

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.SystemFontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TextDragApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDragApp() {

    // Input Text & Text to Display - states
    var showTextInputDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }
    var textToDisplay by remember { mutableStateOf("") }
    var textPosition by remember { mutableStateOf(Offset(0f, 0f)) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var textSize by remember { mutableStateOf(IntSize.Zero) }

    // Fonts & Text Styles - states
    var selectedFont by remember { mutableStateOf(FontFamily.Default) }
    val availableFonts = listOf(
        "Default" to FontFamily.Default,
        "Monospace" to FontFamily.Monospace,
        "Serif" to FontFamily.Serif,
        "Sans-serif" to FontFamily.SansSerif,
        "Cursive" to FontFamily.Cursive
    )
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }
    var isUnderline by remember { mutableStateOf(false) }
    var isStrikethrough by remember { mutableStateOf(false) }

    // Font Size - states
    var fontSize by remember { mutableStateOf(20.sp) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var inputFontSize by remember { mutableStateOf(fontSize.value.toInt().toString()) }
    var inputFontSizeError by remember { mutableStateOf<String?>(null) }
    var showSlider by remember { mutableStateOf(false) }

    // Canvas Color & Text Color - states
    var canvasCol by remember { mutableStateOf(Color.LightGray) }
    var textCol by remember { mutableStateOf(Color.Black) }

    // Dark mode state
    var isDarkMode by remember { mutableStateOf(false) }
    var infoTextCol by remember { mutableStateOf(Color.Black) }
    var colorsBorder by remember { mutableStateOf(Color.Black) }

    // Scroll state
    val scrollState = rememberScrollState()


    //------ Undo Redo states & stacks -----------------------------------------------------------------
    data class ActionStates(
        val textToDisplay: String,
        val selectedFont: FontFamily,
        val isBold: Boolean,
        val isItalic: Boolean,
        val isUnderline: Boolean,
        val isStrikethrough: Boolean,
        val fontSize: TextUnit,
        val textCol: Color,
    )

    val undoStack = remember { mutableStateListOf<ActionStates>() }
    val redoStack = remember { mutableStateListOf<ActionStates>() }

    // Save Action State Function - to save any actions in the history stack
    fun saveActionState() {
        redoStack.clear()
        undoStack.add(
            ActionStates(
                textToDisplay,
                selectedFont,
                isBold,
                isItalic,
                isUnderline,
                isStrikethrough,
                fontSize,
                textCol
            )
        )
    }

    fun applyActionState(state: ActionStates) {
        textToDisplay = state.textToDisplay
        selectedFont = state.selectedFont as SystemFontFamily
        isBold = state.isBold
        isItalic = state.isItalic
        isUnderline = state.isUnderline
        isStrikethrough = state.isStrikethrough
        fontSize = state.fontSize
        textCol = state.textCol
    }

    // Undo function
    fun undo() {
        if (undoStack.isNotEmpty()) {
            val lastAction = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(
                ActionStates(
                    textToDisplay,
                    selectedFont,
                    isBold,
                    isItalic,
                    isUnderline,
                    isStrikethrough,
                    fontSize,
                    textCol
                )
            )
            applyActionState(lastAction)
        }
    }

    // Redo function
    fun redo() {
        if (redoStack.isNotEmpty()) {
            val nextState = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(
                ActionStates(
                    textToDisplay,
                    selectedFont,
                    isBold,
                    isItalic,
                    isUnderline,
                    isStrikethrough,
                    fontSize,
                    textCol
                )
            )
            applyActionState(nextState)
        }
    }

    // Change text color function
    fun changeTextColor(newColor: Color) {
        if (textCol != newColor) {
            saveActionState()
            textCol = newColor
        }
    }

    // Vibrations on button click ------------
    val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager: VibratorManager =
            LocalContext.current.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.getDefaultVibrator()
    } else {
        @Suppress("DEPRECATION")  // backward compatibility for Android API < 31
        LocalContext.current.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun vibrate() {  // Vibrate Function ------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(40)
        }
    }

//--------------------------------------------------------------------------------------------------

    // Main Column
    Column(
        modifier = Modifier
            .fillMaxSize().background(if (isDarkMode) Color.Black else Color.White)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(55.dp))


        // Customized App Name & DarkMode Icon Button
        Row(verticalAlignment = Alignment.CenterVertically) {

            Spacer(modifier = Modifier.size(10.dp))

            Text(
                text = buildAnnotatedString {
                    // T
                    withStyle(
                        style = SpanStyle(
                            fontSize = 28.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = Color.Blue,
                            letterSpacing = 0.5.sp
                        )
                    ) {
                        append("T")
                    }

                    // ext
                    withStyle(
                        style = SpanStyle(
                            color = infoTextCol,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Monospace,
                            textDecoration = TextDecoration.Underline,
                            letterSpacing = 0.5.sp
                        )
                    ) {
                        append("ext")
                    }

                    // E
                    withStyle(
                        style = SpanStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            letterSpacing = 0.4.sp
                        )
                    ) {
                        append(" E")
                    }

                    // ditor
                    withStyle(
                        style = SpanStyle(
                            color = infoTextCol,
                            fontSize = 24.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 0.4.sp
                        )
                    ) {
                        append("ditor ")
                    }

                    // on
                    withStyle(
                        style = SpanStyle(
                            color = infoTextCol,
                            fontSize = 24.sp,
                            textDecoration = TextDecoration.LineThrough,
                            letterSpacing = 0.4.sp
                        )
                    ) {
                        append(" on ")
                    }

                    // Canvas
                    withStyle(
                        style = SpanStyle(
                            color = Color.Magenta,
                            fontSize = 30.sp,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.4.sp
                        )
                    ) {
                        append(" Canvas")
                    }

                },

                fontSize = 24.sp,
                letterSpacing = 0.3.sp,
                color = Color.Black,
            )

            Spacer(modifier = Modifier.size(15.dp))

            // Dark Mode Icon Button
            Button(
                onClick = {
                    isDarkMode = !isDarkMode
                    infoTextCol = if (isDarkMode) Color.White else Color.Black
                    colorsBorder = if (isDarkMode) Color.White else Color.Black
                },
                colors = ButtonDefaults.buttonColors(
                    if (isDarkMode) Color.Black else Color.White
                ),
                border = BorderStroke(0.5.dp, if (isDarkMode) Color.White else Color.Black),
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (isDarkMode) R.drawable.baseline_light_mode_24
                        else R.drawable.baseline_dark_mode_24
                    ),
                    contentDescription = "Dark Mode",
                    tint = if (isDarkMode) Color.White else Color.Black,
                    modifier = Modifier
                        .graphicsLayer {
                            // Flip horizontally if it's the dark mode icon
                            if (!isDarkMode) scaleX = -1f
                        }
                )
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)

        Spacer(modifier = Modifier.height(20.dp))

        // Canvas Colors : Grey, White, Black & Undo, Redo Buttons
        Row(verticalAlignment = Alignment.CenterVertically)
        {
            Text("Canvas :", fontSize = 18.sp, color = infoTextCol)

            Spacer(modifier = Modifier.size(10.dp))

            // Gray button pre-selected
            Button(
                onClick = { if (canvasCol != Color.LightGray) canvasCol = Color.LightGray },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(Color.LightGray),
                border = BorderStroke(
                    if (canvasCol == Color.LightGray) 1.5.dp else 0.5.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (canvasCol == Color.LightGray) 35.dp
                    else 30.dp
                )
            ) {
            }

            Spacer(modifier = Modifier.size(10.dp))

            // White button
            Button(
                onClick = { canvasCol = Color.White },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(Color.White),
                border = BorderStroke(
                    if (canvasCol == Color.White) 1.5.dp else 0.5.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (canvasCol == Color.White) 35.dp
                    else 30.dp
                )
            ) {
            }

            Spacer(modifier = Modifier.size(10.dp))

            // Black button
            Button(
                onClick = { canvasCol = Color.Black },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(Color.Black),
                border = BorderStroke(
                    if (canvasCol == Color.Black) 1.5.dp else 0.4.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (canvasCol == Color.Black) 35.dp
                    else 30.dp
                )
            ) {
            }

            Spacer(modifier = Modifier.size(20.dp))

            // Undo Icon button
            Icon(
                painter = painterResource(id = R.drawable.baseline_undo_24),
                contentDescription = "Undo",
                tint =
                    if (isDarkMode)
                        if (undoStack.isNotEmpty())
                            Color.White
                        else
                            Color.Gray
                    else if (undoStack.isNotEmpty())
                        Color.Black
                    else
                        Color.LightGray,
                modifier = Modifier.size(30.dp)
                    .clickable(
                        enabled = if (undoStack.isNotEmpty()) true else false,
                        onClickLabel = "Undo",
                        role = Role.Button,
                        onClick = {
                            undo()
                            vibrate()
                        }
                    )
            )

            Spacer(modifier = Modifier.size(10.dp))

            // Redo Icon
            Icon(
                painter = painterResource(id = R.drawable.baseline_redo_24),
                contentDescription = "Redo",
                tint =
                    if (isDarkMode)
                        if (redoStack.isNotEmpty())
                            Color.White
                        else
                            Color.Gray
                    else if (redoStack.isNotEmpty())
                        Color.Black
                    else
                        Color.LightGray,
                modifier = Modifier.size(30.dp)
                    .clickable(
                        enabled = if (redoStack.isNotEmpty()) true else false,
                        onClickLabel = "Redo",
                        role = Role.Button,
                        onClick = {
                            redo()
                            vibrate()
                        }
                    )
            )

        }

        Spacer(modifier = Modifier.size(15.dp))


        // Square Canvas
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(canvasCol)
                .border(
                    0.5.dp,
                    if (canvasCol == Color.Black) Color.White
                    else Color.Black
                )
                .onGloballyPositioned { coordinates ->
                    boxSize = coordinates.size
                },
            contentAlignment = Alignment.TopStart
        ) {
            if (textToDisplay.isNotEmpty()) {
                Text(
                    text = textToDisplay,
                    style = TextStyle(
                        fontSize = fontSize,
                        color = textCol,
                        fontFamily = selectedFont,
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration =
                            if (isUnderline)
                                TextDecoration.Underline
                            else if (isStrikethrough)
                                TextDecoration.LineThrough
                            else TextDecoration.None
                    ),
                    modifier = Modifier
                        .offset { IntOffset(textPosition.x.toInt(), textPosition.y.toInt()) }
                        .onGloballyPositioned { coordinates ->
                            textSize = coordinates.size
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                var newX = textPosition.x + dragAmount.x
                                var newY = textPosition.y + dragAmount.y

                                // Apply constraints for each side of the box
                                // Left boundary
                                if (newX < 0f) newX = 0f
                                //Right boundary
                                if (newX + textSize.width > boxSize.width) {
                                    newX = (boxSize.width - textSize.width).toFloat()
                                }
                                // Top boundary
                                if (newY < 0f) newY = 0f
                                // Bottom boundary
                                if (newY + textSize.height > boxSize.height) {
                                    newY = (boxSize.height - textSize.height).toFloat()
                                }

                                textPosition = Offset(newX, newY) // Update the text position
                            }
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Add-Edit Text Button, Delete Text Button & Center Text Align Button
        Row(verticalAlignment = Alignment.CenterVertically) {

            // Add-Edit Text Button
            Button(
                onClick = {
                    showTextInputDialog = true
                },
                colors = ButtonDefaults.buttonColors(Color.Black),
                border = BorderStroke(0.5.dp, Color.White)
            ) {
                Text(
                    if (textToDisplay.isEmpty()) "Add Text" else "Edit Text",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            // Delete Text Button
            Button(
                onClick = {
                    if (textToDisplay.isNotEmpty()) {
                        saveActionState()
                        isBold = false
                        isItalic = false
                        isUnderline = false
                        isStrikethrough = false
                        fontSize = 20.sp
                        textCol = Color.Black
                    }
                    textToDisplay = ""
                    vibrate()
                },
                colors =
                    if (textToDisplay.isEmpty())
                        if (isDarkMode)
                            ButtonDefaults.buttonColors(Color.Black)
                        else
                            ButtonDefaults.buttonColors(Color.Gray)
                    else
                        ButtonDefaults.buttonColors(Color.Black),
                border = BorderStroke(
                    0.5.dp,
                    if (isDarkMode)
                        if (textToDisplay.isEmpty())
                            Color.Gray
                        else
                            Color.White
                    else
                        Color.White
                )
            ) {
                Text(
                    "Delete Text",
                    color =
                        if (isDarkMode)
                            if (textToDisplay.isEmpty())
                                Color.Gray
                            else
                                Color.White
                        else
                            Color.White,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            // Center Text Align - Icon Button
            Button(
                onClick = {
                    if (textToDisplay.isNotEmpty()) {
                        saveActionState()
                        textPosition = Offset(
                            ((boxSize.width - textSize.width) / 2).toFloat(),
                            ((boxSize.height - textSize.height) / 2).toFloat()
                        )
                        vibrate()
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Black),
                border = BorderStroke(0.5.dp, Color.White),
                shape = CircleShape,
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_format_align_center_24),
                    contentDescription = "Center Text",
                    tint = Color.White
                )
            }

        }

        Spacer(modifier = Modifier.height(10.dp))

        // Fonts, Bold, Italic, Underline, Strikethrough
        Row(verticalAlignment = Alignment.CenterVertically) {

            // Font Dropdown
            DropdownButton(
                textToDisplay = textToDisplay,
                options = availableFonts,
                selectedOption = selectedFont,
                onOptionSelected = { selectedFont = it.second as SystemFontFamily },
                saveActionState = { saveActionState() }
            )

            Spacer(modifier = Modifier.size(15.dp))

            // Bold
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) {
                        saveActionState()
                        isBold = !isBold
                        vibrate()
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Black),
                border = BorderStroke(
                    if (isBold) 1.5.dp else 0.5.dp,
                    Color.White
                ),
                shape = CircleShape,
                modifier = Modifier.size(if (isBold) 45.dp else 40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("B", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.size(15.dp))

            //Italic
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) {
                        saveActionState()
                        isItalic = !isItalic
                        vibrate()
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Black),
                border = BorderStroke(
                    if (isItalic) 1.5.dp else 0.5.dp,
                    Color.White
                ),
                shape = CircleShape,
                modifier = Modifier.size(if (isItalic) 45.dp else 40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "I",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.offset((-2).dp, 0.dp)
                )
            }

            Spacer(modifier = Modifier.size(15.dp))

            // Underline
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) {
                        saveActionState()
                        if (isStrikethrough) {
                            isStrikethrough = !isStrikethrough
                        }
                        isUnderline = !isUnderline
                        vibrate()
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Black),
                border = BorderStroke(
                    if (isUnderline) 1.5.dp else 0.5.dp,
                    Color.White
                ),
                shape = CircleShape,
                modifier = Modifier.size(if (isUnderline) 45.dp else 40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "U",
                    color = Color.White,
                    fontSize = 16.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.offset(0.dp, (-2).dp)
                )
            }

            Spacer(modifier = Modifier.size(15.dp))

            // Strikethrough / Linethrough
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) {
                        saveActionState()
                        if (isUnderline)
                            isUnderline = !isUnderline
                        isStrikethrough = !isStrikethrough
                        vibrate()
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Black),
                border = BorderStroke(
                    if (isStrikethrough) 1.5.dp else 0.5.dp,
                    Color.White
                ),
                shape = CircleShape,
                modifier = Modifier.size(if (isStrikethrough) 45.dp else 40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "S",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    textDecoration = TextDecoration.LineThrough,
                    fontWeight = FontWeight.Medium
                )
            }

        }

        Spacer(modifier = Modifier.height(10.dp))

        // Text Size Controls - +, size display & reset
        Row(verticalAlignment = Alignment.CenterVertically) {

            // Slider visibility toggle icon button
            Icon(
                painter = painterResource(id = R.drawable.slidericon),
                tint = if(isDarkMode) Color.White else Color.Black,
                contentDescription = "Slider Visibility",
                modifier = Modifier.clickable(
                    enabled = true,
                    onClickLabel = "Slider Visibility",
                    role = Role.Button,
                    onClick = {
                        if(textToDisplay.isNotEmpty()) {
                            if(showSlider == false) {
                                saveActionState()
                            }
                            showSlider = !showSlider
                            vibrate()
                        }
                    }
                ).size(35.dp)
            )

            Spacer(modifier = Modifier.size(15.dp))

            // - Button
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) {
                        saveActionState()
                        fontSize = (fontSize.value - 2).sp
                        vibrate()
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Black),
                border = BorderStroke(0.5.dp, Color.White),
                shape = CircleShape,
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("-", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.size(15.dp))

            // Text Size Display
            Text(
                text = fontSize.value.toInt().toString(),
                fontSize = 18.sp, color = infoTextCol,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable(
                        enabled = true,
                        onClickLabel = "Text Size",
                        role = Role.Button,
                        onClick = {
                            // onclick to show Dialog of input text size
                            if(textToDisplay.isNotEmpty()) {
                                showFontSizeDialog = true
                            }
                        }
                    )
            )

            Spacer(modifier = Modifier.size(15.dp))

            // + Button
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) {
                        saveActionState()
                        fontSize = (fontSize.value + 2).sp
                        vibrate()
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Black),
                border = BorderStroke(0.5.dp, Color.White),
                shape = CircleShape,
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("+", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.size(15.dp))

            // Reset text size button
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) {
                        if (fontSize != 20.sp)
                            saveActionState()
                        fontSize = 20.sp
                        vibrate()
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Black),
                border = BorderStroke(0.5.dp, Color.White),
                contentPadding = PaddingValues(start = 10.dp, end = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_replay_24),
                        contentDescription = "Reset Text Size",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Text("Reset", fontSize = 16.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Text size seekbar slider
        if(showSlider && textToDisplay.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Slider(
                    enabled = if (textToDisplay.isNotEmpty()) true else false,
                    value = fontSize.value,
                    valueRange = 5f..200f,
                    onValueChange = {
                        fontSize = it.sp
                    },
                    onValueChangeFinished = {
                        saveActionState()
                        fontSize = (fontSize.value).sp
                        vibrate()
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Black,
                        activeTrackColor = if(isDarkMode) Color.White else Color.Black,
                        inactiveTrackColor = Color.Gray
                    ),
                    thumb = { // show font size inside the thumb
                        Box(
                            modifier = Modifier
                                .size(width = 45.dp, height = 30.dp)
                                .background(
                                    color = if (textToDisplay.isNotEmpty()) Color.Black else Color.Gray,
                                    shape = RoundedCornerShape(25.dp)
                                ).border(
                                    // border with rounded corner shape
                                    width = 1.dp,
                                    color = if (isDarkMode) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(25.dp)
                                )
                                .padding(horizontal = 5.dp)
                        ) {
                            Text(
                                text = fontSize.value.toInt().toString(),
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Text Colors - Row 1
        Row(verticalAlignment = Alignment.CenterVertically) {

            Text("Text Colors : ", fontSize = 18.sp, color = infoTextCol)

            Spacer(modifier = Modifier.size(10.dp))

            // Black color
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) changeTextColor(Color.Black)
                },
                colors = ButtonDefaults.buttonColors(Color.Black),
                shape = CircleShape,
                border = BorderStroke(
                    if (textCol == Color.Black) 1.5.dp else 0.4.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (textCol == Color.Black) 35.dp
                    else 30.dp
                )
            ) {
            }

            Spacer(modifier = Modifier.size(15.dp))

            // White color
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) changeTextColor(Color.White)
                },
                colors = ButtonDefaults.buttonColors(Color.White),
                shape = CircleShape,
                border = BorderStroke(
                    if (textCol == Color.White) 1.5.dp else 0.5.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (textCol == Color.White) 35.dp
                    else 30.dp
                )
            ) {
            }

            Spacer(modifier = Modifier.size(15.dp))

            // Light Gray color
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) changeTextColor(Color.LightGray)
                },
                colors = ButtonDefaults.buttonColors(Color.LightGray),
                shape = CircleShape,
                border = BorderStroke(
                    if (textCol == Color.LightGray) 1.5.dp else 0.5.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (textCol == Color.LightGray) 35.dp
                    else 30.dp
                )
            ) {
            }

            Spacer(modifier = Modifier.size(15.dp))

            // Gray color
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) changeTextColor(Color.Gray)
                },
                colors = ButtonDefaults.buttonColors(Color.Gray),
                shape = CircleShape,
                border = BorderStroke(
                    if (textCol == Color.Gray) 1.5.dp else 0.5.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (textCol == Color.Gray) 35.dp
                    else 30.dp
                )
            ) {
            }

        }

        Spacer(modifier = Modifier.size(15.dp))

        // Text Colors - Row 2
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            // Red color
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) changeTextColor(Color.Red)
                },
                colors = ButtonDefaults.buttonColors(Color.Red),
                shape = CircleShape,
                border = BorderStroke(
                    if (textCol == Color.Red) 1.5.dp else 0.5.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (textCol == Color.Red) 35.dp
                    else 30.dp
                )
            ) {
            }


            // Yellow color
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) changeTextColor(Color.Yellow)
                },
                colors = ButtonDefaults.buttonColors(Color.Yellow),
                shape = CircleShape,
                border = BorderStroke(
                    if (textCol == Color.Yellow) 1.5.dp else 0.5.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (textCol == Color.Yellow) 35.dp
                    else 30.dp
                )
            ) {
            }

            // Orange color - FF8000
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) changeTextColor(Color(0xFFFF8000))
                },
                colors = ButtonDefaults.buttonColors(Color(0xFFFF8000)),
                shape = CircleShape,
                border = BorderStroke(
                    if (textCol == Color(0xFFFF8000)) 1.5.dp else 0.5.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (textCol == Color(0xFFFF8000)) 35.dp
                    else 30.dp
                )
            ) {
            }


            // Blue color
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) changeTextColor(Color.Blue)
                },
                colors = ButtonDefaults.buttonColors(Color.Blue),
                shape = CircleShape,
                border = BorderStroke(
                    if (textCol == Color.Blue) 1.5.dp else 0.5.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (textCol == Color.Blue) 35.dp
                    else 30.dp
                )
            ) {
            }


            // Cyan color
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) changeTextColor(Color.Cyan)
                },
                colors = ButtonDefaults.buttonColors(Color.Cyan),
                shape = CircleShape,
                border = BorderStroke(
                    if (textCol == Color.Cyan) 1.5.dp else 0.5.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (textCol == Color.Cyan) 35.dp
                    else 30.dp
                )
            ) {
            }


            // Green color - 00CC00
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) changeTextColor(Color(0xFF00CC00))
                },
                colors = ButtonDefaults.buttonColors(Color(0xFF00CC00)),
                shape = CircleShape,
                border = BorderStroke(
                    if (textCol == Color(0xFF00CC00)) 1.5.dp else 0.5.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (textCol == Color(0xFF00CC00)) 35.dp
                    else 30.dp
                )
            ) {
            }


            // Magenta color
            Button(
                onClick = {
                    if(textToDisplay.isNotEmpty()) changeTextColor(Color.Magenta)
                },
                colors = ButtonDefaults.buttonColors(Color.Magenta),
                shape = CircleShape,
                border = BorderStroke(
                    if (textCol == Color.Magenta) 1.5.dp else 0.5.dp,
                    colorsBorder
                ),
                modifier = Modifier.size(
                    if (textCol == Color.Magenta) 35.dp
                    else 30.dp
                )
            ) {
            }
        }


        // Text Input Dialog
        if (showTextInputDialog) {
            TextInputDialog(
                initialText = inputText,
                onInputTextChange = { inputText = it },
                onConfirm = {
                    if (inputText.isBlank()) {
                        // Show an error or handle invalid input
                        inputError = "Text cannot be empty or spaces only"
                    } else {
                        saveActionState()
                        textToDisplay = inputText
                        showTextInputDialog = false
                        textPosition = Offset(
                            ((boxSize.width - textSize.width) / 2).toFloat(),
                            ((boxSize.height - textSize.height) / 2).toFloat()
                        )
                    }
                },
                onDismiss = { showTextInputDialog = false },
                inputError = inputError
            )
        }

        // Font Size Input Dialog
        if (showFontSizeDialog) {

            // Use TextFieldValue to control cursor position
            var textFieldValue by remember(inputFontSize) {
                mutableStateOf(
                    TextFieldValue(
                        text = inputFontSize,
                        selection = TextRange(inputFontSize.length) // Cursor at end
                    )
                )
            }
            // Create FocusRequester and keyboard controller
            val focusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current

            AlertDialog(
                onDismissRequest = { showFontSizeDialog = false },
                title = { Text("Enter Font Size", fontSize = 16.sp) },
                text = {
                    Column {
                        TextField(
                            value = textFieldValue,
                            onValueChange = { newValue ->
                                textFieldValue = newValue
                                inputFontSize = newValue.text // Update raw text
                                inputFontSizeError = when {
                                    newValue.text.toIntOrNull() == null -> "Invalid number"
                                    newValue.text.toIntOrNull() !in 5..200 -> "Enter a number between 5 to 200 only."
                                    else -> null
                                }
                            },
                            label = { Text("5 to 200") },
                            isError = inputFontSizeError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier // Request focus when the dialog appears
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) { // Show keyboard when focused
                                        keyboardController?.show()
                                    }
                                }
                        )
                        if (inputFontSizeError != null) {
                            Text(
                                text = inputFontSizeError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    IconButton(onClick = {
                        if (inputFontSize.toIntOrNull() in 5..200) {
                            saveActionState()
                            fontSize = inputFontSize.toInt().sp
                            showFontSizeDialog = false
                        } else {
                            inputFontSizeError = "Enter a number between 5 to 200 only."
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Confirm")
                    }
                },
                dismissButton = {
                    IconButton(onClick = {
                        showFontSizeDialog = false
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss")
                    }
                }
            )
            // Auto-focus when the dialog is displayed
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }


}


@Composable
fun DropdownButton(
    textToDisplay: String,
    options: List<Pair<String, FontFamily>>,
    selectedOption: FontFamily,
    onOptionSelected: (Pair<String, FontFamily>) -> Unit,
    saveActionState: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(
            onClick = { if(textToDisplay.isNotEmpty()) expanded = !expanded },
            colors = ButtonDefaults.buttonColors(Color.Black),
            border = BorderStroke(0.5.dp, Color.White)
        ) {
            Text("Fonts  ›", fontSize = 16.sp, color = Color.White)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.first) },
                    onClick = {
                        saveActionState()
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun TextInputDialog(
    initialText: String,
    onInputTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    inputError: String?,
) {
    // Use TextFieldValue instead of String to control cursor position
    var textFieldValue by remember(initialText) {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length) // Cursor at the end
            )
        )
    }

    // Create FocusRequester and keyboard controller
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Text") },
        text = {
            Column {
                TextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onInputTextChange(newValue.text) // Pass only the raw text back
                    },
                    label = { Text("Text") },
                    isError = inputError != null,
                    modifier = Modifier // Request focus when the dialog appears
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) { // Show keyboard when focused
                                keyboardController?.show()
                            }
                        }
                )
                if (inputError != null) {
                    Text(
                        text = inputError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            IconButton(onClick = onConfirm) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Confirm")
            }
        },
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    )
    // Auto-focus when the dialog is displayed
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TextDragApp()
}
