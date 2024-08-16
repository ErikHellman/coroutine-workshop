package se.hellsoft.channels

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.receiveAsFlow
import okio.Buffer

fun main() = runBlocking {
    producerConsumer()
//    consumeAndRespond()
}

suspend fun producerConsumer() = coroutineScope {
    val channel = Channel<Int>()

    val consumer1 = simpleConsumer("1", channel)
    val consumer2 = simpleConsumer("2", channel)
    val consumer3 = simpleConsumer("3", channel)

    val producer = launch {
        for (i in 1..10) {
            println("Sending $i")
            channel.send(i)
        }
        println("Closing channel!")
        channel.close()
    }


    producer.join()
    consumer1.join()
//    consumer2.join()
//    consumer3.join()

    println("Producer and all Consumers are done.")
}

private fun CoroutineScope.simpleConsumer(name: String, channel: Channel<Int>) = launch {
    delay(500)
    channel.consumeEach {
        println("Consumer $name received $it")
        delay(500)
    }
    println("Consumer $name is done.")
}

suspend fun consumeAndRespond() = coroutineScope {
    val channel = Channel<CompletableDeferred<Int>>()

    val producer = launch {
        for (i in 1..10) {
            println("Sending $i")
            val response = CompletableDeferred<Int>()
            channel.send(response)
            println("Response: ${response.await()}")
        }
        println("Closing channel!")
        channel.close()
    }

    val consumer = launch {
        var i = 0
        channel.consumeEach {
            delay(100)
            i++
            it.complete(i)
        }
    }

    producer.join()
    consumer.join()

    println("Producer and all Consumers are done.")
}