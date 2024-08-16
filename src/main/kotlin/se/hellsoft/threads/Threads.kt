package se.hellsoft.threads

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.Executors
import kotlin.time.measureTime

fun main() {
//    testCPUthreads()
    testIOthreads()
}

fun testCPUthreads() {
    val count = Runtime.getRuntime().availableProcessors()
    val executorService = Executors.newFixedThreadPool(count)

    val elapsedTime = measureTime {
        val futures = (0 until count).map { i ->
            executorService.submit {
                val result = fibonacci(45L)
                println("Thread $i got $result")
            }
        }

        // Wait for all tasks to complete
        futures.forEach { it.get() }
    }

    println("All tasks completed in $elapsedTime")
    executorService.shutdown()
}

private fun testIOthreads() {
    val count = 1000
    val httpClient = OkHttpClient().newBuilder().build()
    val executorService = Executors.newFixedThreadPool(count)
    val request = Request.Builder().get().url("http://localhost:8080/delay/2").build()

    val elapsedTime = measureTime {
        val futures = (1..count).map { i ->
            executorService.submit {
                val response = httpClient.newCall(request.newBuilder().build()).execute()
                if (response.isSuccessful) {
                    println("$i done")
                } else {
                    println("$i failed")
                }
            }
        }

        futures.forEach { it.get() }
    }

    println("All tasks completed in $elapsedTime")
    executorService.shutdown()
}

fun fibonacci(n: Long): Long {
    return if (n <= 1) {
        n
    } else {
        fibonacci(n - 1) + fibonacci(n - 2)
    }
}
