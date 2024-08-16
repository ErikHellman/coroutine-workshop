package se.hellsoft.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import se.hellsoft.threads.fibonacci
import java.io.IOException
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main() = runBlocking {
}

private fun forcast() {
    val time = measureTimeMillis {
        runBlocking {
            println("Weather forecast")
            launch {
                printForecast()
            }
            launch {
                printTemperature()
            }
        }
    }

    println("Execution time: ${time / 1000.0} seconds")
}

suspend fun printForecast() {
    delay(Random.nextLong(900, 1100))
    println("Sunny")
}

suspend fun printTemperature() {
    delay(Random.nextLong(900, 1100))
    println("30\u00b0C")
}


suspend fun coFibonacci() = suspendCoroutine { continuation ->
    val result = fibonacci(45)
    continuation.resume(result)
}

suspend fun illegalStateCall(): Unit = suspendCoroutine { continuation ->
    thread {
        sleep(2000)
        try {
            println("Resume!")
            continuation.resume(Unit)
        } catch (e: Exception) {
            println("Resume with exception!")
            continuation.resumeWithException(e)
            e.printStackTrace()
        }
    }
}

suspend fun suspendingIoCall(): String = suspendCancellableCoroutine { continuation ->
    val httpClient = OkHttpClient().newBuilder().build()
    val request = Request.Builder().get().url("http://localhost:8080/delay/2").build()
    val call = httpClient.newCall(request)

    val callback = object : Callback {
        override fun onResponse(call: Call, response: Response) {
            if (continuation.isActive) {
                println("onResponse!")
                response.body?.string()?.let {
                    continuation.resume(it)
                } ?: continuation.resumeWithException(IOException(response.message))
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            if (continuation.isActive) {
                println("onFailure!")
                continuation.resumeWithException(e)
            }
        }
    }

    call.enqueue(callback)

    continuation.invokeOnCancellation {
        call.cancel()
        println("Cancelled!")
    }
}
