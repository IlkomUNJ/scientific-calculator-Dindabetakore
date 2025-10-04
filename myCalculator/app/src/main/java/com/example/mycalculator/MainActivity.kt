package com.example.mycalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.*
import androidx.compose.ui.Modifier
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mycalculator.ui.theme.MyCalculatorTheme
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import java.text.DecimalFormat
import kotlin.math.*
import kotlin.text.replace

val DarkBlue = Color(0xFF0D1B2A)
val DeepBlue = Color(0xFF0D1B2A)
val Blue = Color(0xFF205B7A)
val Pink= Color (0xFFE91E63)
val White = Color.White
val Black = Color.Black


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyCalculatorTheme {
                CalculatorApp()
            }
        }
    }
}
class CalculatorEngine {

    private val factorial = object : Function("fact", 1) {
        override fun apply(vararg args: Double): Double {
            val n = args[0].toInt()
            if (n < 0 || n != args[0].toInt()) {
                throw IllegalArgumentException("Argument factorial harus integer >= 0")
            }
            return (1..n).fold(1.0) { acc, i -> acc * i }
        }
    }
    fun evaluate(exp: String): String {
        return try {
            var sanitized = exp
                .replace("×", "*")
                .replace("÷", "/")
                .replace("√", "sqrt")
                .replace("xʸ", "^")

            val openCount = sanitized.count { it == '(' }
            val closeCount = sanitized.count { it == ')' }

            if (openCount > closeCount) {
                repeat(openCount - closeCount) {
                    sanitized += ")"
                }
            }

            val sin = object : Function("sin", 1) {
                override fun apply(vararg args: Double): Double = kotlin.math.sin(Math.toRadians(args[0]))
            }
            val cos = object : Function("cos", 1) {
                override fun apply(vararg args: Double): Double = kotlin.math.cos(Math.toRadians(args[0]))
            }
            val tan = object : Function("tan", 1) {
                override fun apply(vararg args: Double): Double = kotlin.math.tan(Math.toRadians(args[0]))
            }
            val log = object : Function("log", 1) {
                override fun apply(vararg args: Double): Double = kotlin.math.log10(args[0])
            }
            val ln = object : Function("ln", 1) {
                override fun apply(vararg args: Double): Double = kotlin.math.ln(args[0])
            }

            val asin = object : Function("asin", 1) {
                override fun apply(vararg args: Double): Double = Math.toDegrees(kotlin.math.asin(args[0]))
            }
            val acos = object : Function("acos", 1) {
                override fun apply(vararg args: Double): Double = Math.toDegrees(kotlin.math.acos(args[0]))
            }
            val atan = object : Function("atan", 1) {
                override fun apply(vararg args: Double): Double = Math.toDegrees(kotlin.math.atan(args[0]))
            }

            val expression = ExpressionBuilder(sanitized)
                .function(factorial)
                .function(sin)
                .function(cos)
                .function(tan)
                .function(log)
                .function(ln)
                .function(asin)
                .function(acos)
                .function(atan)
                .build()

            val result = expression.evaluate()
            val df = DecimalFormat("#.########")
            df.format(result)
        } catch (e: Exception) {
            "Error"
        }
    }
}

class CalculatorState {
    var display by mutableStateOf("0")
    var expression by mutableStateOf("")
    var isInverse by mutableStateOf(false)

    fun clear() {
        display = "0"
        expression = ""
    }

    fun backspace() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            display = if (expression.isEmpty()) "0" else expression
        }
    }
}

/* UI KALKULATOR */
@Composable
fun CalculatorApp() {
    val engine = remember { CalculatorEngine() }
    val state = remember { CalculatorState() }
    var showScientific by remember { mutableStateOf(false) }
    var isInverse by remember { mutableStateOf(false) }

    fun handleInput(button: String) {
        when (button) {
            "AC" -> state.clear()
            "⌫" -> state.backspace()
            "=" -> {
                val result = engine.evaluate(state.expression)
                state.display = result
                state.expression = if (result != "Error") result else ""
            }
            "inv" -> {
                isInverse = !isInverse
            }
            "%" -> {
                if (state.expression.isNotEmpty() && state.expression.last().isDigit()) {
                    val idx = state.expression.indexOfLast { !it.isDigit() && it != '.' }
                    val numberPart = state.expression.substring(idx + 1)
                    val before = state.expression.substring(0, idx + 1)
                    state.expression = before + "(${numberPart}/100)"
                    state.display += "%"
                }
            }
            "1/x" -> {
                if (state.expression.isNotEmpty() && state.expression != "0") {
                    state.expression = "1/(${state.expression})"
                    state.display = "1/(${state.display})"
                }
            }
            else -> {
                when (button) {
                    "sin" -> {
                        val func = if (isInverse) "asin(" else "sin("
                        val disp = if (isInverse) "sin⁻¹(" else "sin("
                        state.expression += func
                        state.display += disp
                    }
                    "cos" -> {
                        val func = if (isInverse) "acos(" else "cos("
                        val disp = if (isInverse) "cos⁻¹(" else "cos("
                        state.expression += func
                        state.display += disp
                    }
                    "tan" -> {
                        val func = if (isInverse) "atan(" else "tan("
                        val disp = if (isInverse) "tan⁻¹(" else "tan("
                        state.expression += func
                        state.display += disp
                    }
                    "xʸ" -> {
                        state.expression += "^"
                        state.display += "^"
                    }
                    "x!" -> {
                        if (state.expression.isNotEmpty() && state.expression.last().isDigit()) {
                            val idx = state.expression.indexOfLast { !it.isDigit() && it != '.' }
                            val numberPart = state.expression.substring(idx + 1)
                            val before = state.expression.substring(0, idx + 1)
                            state.expression = before + "fact($numberPart)"
                            state.display += "!"
                        }
                    }
                    else -> {
                        if (state.display == "0" || state.display == "Error") {
                            state.expression = button
                            state.display = button
                        } else {
                            state.expression += button
                            state.display += button
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(8.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = state.display,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            fontSize = if (state.display.length > 9) 48.sp else 72.sp,
            fontWeight = FontWeight.Light,
            color = White,
            textAlign = TextAlign.End,
            maxLines = 2
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("7", "8", "9").forEach { label ->
                CalculatorButton(
                    text = label,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { handleInput(label) }
                )
            }
            listOf("AC", "⌫").forEach { label ->
                CalculatorButton(
                    text = label,
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    onClick = { handleInput(label) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("4", "5", "6", "%").forEach { label ->
                CalculatorButton(
                    text = label,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { handleInput(label) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("1", "2", "3").forEach { label ->
                CalculatorButton(
                    text = label,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { handleInput(label) }
                )
            }
            listOf("×", "÷").forEach { label ->
                CalculatorButton(
                    text = label,
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    onClick = { handleInput(label) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tombol . dan 0 (lebar normal)
            listOf(".", "0").forEach { label ->
                CalculatorButton(
                    text = label,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { handleInput(label) }
                )
            }

            // Tombol + dan - (masing-masing setengah)
            listOf("+", "-").forEach { label ->
                CalculatorButton(
                    text = label,
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    onClick = { handleInput(label) }
                )
            }

            CalculatorButton(
                text = "=",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { handleInput("=") }
            )
        }
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { showScientific = !showScientific },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue,
                contentColor = White
            )
        ) {
            Text(if (showScientific) "˅" else "˄", fontSize = 20.sp)
        }

        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(visible = showScientific) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val sciButtons = listOf(
                    listOf("inv", "sin", "cos", "tan","√"),
                    listOf("ln", "log", "x!", "xʸ", "1/x")
                )
                sciButtons.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { label ->
                            val displayLabel = if (isInverse) {
                                when (label) {
                                    "sin" -> "sin⁻¹"
                                    "cos" -> "cos⁻¹"
                                    "tan" -> "tan⁻¹"
                                    else -> label
                                }
                            } else {
                                label
                            }

                            CalculatorButton(
                                text = displayLabel,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                onClick = {
                                    if (label == "inv") {
                                        isInverse = !isInverse
                                    } else {
                                        handleInput(label)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (bgColor, textColor) = when (text) {
        "⌫"-> DeepBlue to White
        "AC"-> DeepBlue to Pink
        "÷", "×", "-", "+", "%" -> Blue to White
        "inv" -> Blue to White
        "="-> Pink to White
        else -> DarkBlue to White
    }

    ElevatedButton(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor
        ),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
