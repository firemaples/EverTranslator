package tw.firemaples.onscreenocr.floatings.compose.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

@Composable
fun <T> Flow<T>.collectOnLifecycleResumed(state: (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(this, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            this@collectOnLifecycleResumed.collect(state)
        }
    }
}

suspend fun <T> MutableSharedFlow<T>.awaitForSubscriber() {
    subscriptionCount.first { it > 0 }
}