package dev.drekt.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.drekt.core.SideEffectHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CounterScreen()
                }
            }
        }
    }
}

@Composable
fun CounterScreen(vm: CounterViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (state.loading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
        }

        Text(
            text = "${state.count}",
            fontSize = 64.sp,
        )

        Spacer(Modifier.height(24.dp))

        Row {
            Button(onClick = vm::decrement) { Text("-") }
            Spacer(Modifier.width(16.dp))
            Button(onClick = vm::increment) { Text("+") }
        }

        Spacer(Modifier.height(16.dp))

        Row {
            OutlinedButton(onClick = vm::reset) { Text("Reset") }
            Spacer(Modifier.width(16.dp))
            OutlinedButton(
                onClick = vm::loadRandom,
                enabled = !state.loading,
            ) { Text("Random") }
        }

        state.message?.let { msg ->
            Spacer(Modifier.height(16.dp))
            Text(text = msg, color = MaterialTheme.colorScheme.error)
        }
    }
}
