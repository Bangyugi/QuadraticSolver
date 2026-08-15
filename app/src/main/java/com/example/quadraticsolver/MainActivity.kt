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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quadraticsolver.ui.theme.QuadraticSolverTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuadraticSolverTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    QuadraticSolverScreen(
                        onSolve = { a, b, c ->
                            withContext(Dispatchers.IO) {
                                QuadraticNativeLib.solveQuadratic(a, b, c)
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun QuadraticSolverScreen(onSolve: suspend (Double, Double, Double) -> QuadraticResult, modifier: Modifier = Modifier) {
    var aText by remember { mutableStateOf("") }
    var bText by remember { mutableStateOf("") }
    var cText by remember { mutableStateOf("") }
    var resultObj by remember { mutableStateOf<QuadraticResult?>(null) }

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
                resultObj = null

                scope.launch {
                    resultObj = onSolve(a, b, c)
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
        } else if (resultObj != null) {
            val res = resultObj!!
            val displayMessage = when (res.rootType) {
                RootType.INFINITE_ROOTS -> stringResource(R.string.result_infinite_roots)
                RootType.NO_REAL_ROOTS -> stringResource(R.string.result_no_real_roots)
                RootType.ONE_REAL_ROOT -> stringResource(R.string.result_one_real_root, res.x1)
                RootType.DOUBLE_ROOT -> stringResource(R.string.result_double_root, res.x1)
                RootType.TWO_REAL_ROOTS -> stringResource(R.string.result_two_real_roots, res.x1, res.x2)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = displayMessage,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}