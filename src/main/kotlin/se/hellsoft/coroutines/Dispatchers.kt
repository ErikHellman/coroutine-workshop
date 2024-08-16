package se.hellsoft.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep

fun main() = runBlocking(Dispatchers.Default) {
    val result = withContext(Dispatchers.IO) {
        dispatchersDemo()
    }
    println(result)
}

suspend fun dispatchersDemo(): String {
    return veryExpensiveCall()
}

fun veryExpensiveCall(): String {
    sleep(3000)
    return "Hello"
}

