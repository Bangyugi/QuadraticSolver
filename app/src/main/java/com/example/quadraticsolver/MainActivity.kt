package com.example.quadraticsolver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quadraticsolver.ui.theme.QuadraticSolverTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    private external fun solveQuadraticFromNative(a: Double, b: Double, c: Double): DoubleArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuadraticSolverTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    QuadraticSolverScreen(
                        onSolve = { a, b, c ->
                            withContext(Dispatchers.IO) {
                                val roots = solveQuadraticFromNative(a, b, c)
                                formatResult(a, b, c, roots)
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun formatResult(a: Double, b: Double, c: Double, roots: DoubleArray): String {
        val eps = 1e-9
        fun formatNumber(value: Double): String {
            return if (abs(value - value.toLong()) < eps) {
                value.toLong().toString()
            } else {
                String.format(Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')
            }
        }

        return when {
            roots.size == 1 && roots[0].isInfinite() -> {
                "Phương trình vô số nghiệm."
            }
            roots.isEmpty() -> {
                "Phương trình vô nghiệm."
            }
            roots.size == 1 -> {
                if (abs(a) < eps) {
                    "Phương trình có 1 nghiệm:\nx = ${formatNumber(roots[0])}"
                } else {
                    "Phương trình có nghiệm kép:\nx₁ = x₂ = ${formatNumber(roots[0])}"
                }
            }
            roots.size == 2 -> {
                "Phương trình có 2 nghiệm phân biệt:\nx₁ = ${formatNumber(roots[0])}\nx₂ = ${formatNumber(roots[1])}"
            }
            else -> "Không xác định được nghiệm."
        }
    }

    companion object {
        init {
            System.loadLibrary("quadraticsolver")
        }
    }
}

@Composable
fun QuadraticSolverScreen(onSolve: suspend (Double, Double, Double) -> String, modifier: Modifier = Modifier) {
    var aText by remember { mutableStateOf("") }
    var bText by remember { mutableStateOf("") }
    var cText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Giải Phương Trình Bậc 2 (JNI C++)",
            fontSize = 20.sp,
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = aText,
            onValueChange = { aText = it },
            label = { Text("Nhập hệ số a") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bText,
            onValueChange = { bText = it },
            label = { Text("Nhập hệ số b") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cText,
            onValueChange = { cText = it },
            label = { Text("Nhập hệ số c") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val a = aText.toDoubleOrNull() ?: 0.0
                val b = bText.toDoubleOrNull() ?: 0.0
                val c = cText.toDoubleOrNull() ?: 0.0

                isLoading = true
                resultText = ""

                scope.launch {
                    resultText = onSolve(a, b, c)
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Giải Phương Trình")
        }

        if (isLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Đang tính toán trong C++...",
                        fontSize = 16.sp
                    )
                }
            }
        } else if (resultText.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = resultText,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}