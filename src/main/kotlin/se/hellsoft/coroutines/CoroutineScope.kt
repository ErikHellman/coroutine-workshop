package se.hellsoft.coroutines

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Thread.sleep
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random

@ExperimentalCoroutinesApi
fun main() {
    demoScopeCancellation()
}

fun demoCoroutineScope() = runBlocking {
    val job = launch {
        delay(1000L)
        println("Task from runBlocking")
    }

    coroutineScope {
        launch {
            delay(2000L)
            println("Task from nested launch")
        }

        delay(500L)
        println("Task from coroutine scope")
    }

    println("Coroutine scope is over")
}

fun demoScopeCancellation() {
    val context = newFixedThreadPoolContext(5, "coroutine scope")
    val scope = CoroutineScope(context)
    scope.launch {
        println(makeApiCalls())
    }
    sleep(1100)
    scope.cancel()
    println("Cancelled!")
    sleep(10_000)
}

suspend fun makeApiCalls(): String = coroutineScope {
    val call1: Deferred<String> = async { apiCall("delay/1") }
    val call2 = async { apiCall("delay/1") }
    val call3 = async { apiCall("delay/2") }
    val call4 = async { apiCall("delay/2") }

    call1.await() + call2.await() + call3.await() + call4.await()
}

suspend fun apiCall(api:String) = suspendCancellableCoroutine {
    try {
        val httpClient = OkHttpClient().newBuilder().build()
        val request = Request.Builder().get().url("http://localhost:8080/$api").build()
        val call = httpClient.newCall(request)
        val response = call.execute()
        println("Done $api!")
        it.resume(response.body!!.string())
    } catch (e: Exception) {
        println("Failed $api!")
        it.resumeWithException(e)
    }
}