package se.hellsoft.flow

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import okhttp3.*
import java.io.InputStream

fun main() = runBlocking {
    demoCallbackFlow()
}

suspend fun simpleFlow() = coroutineScope {
    val myFlow: SharedFlow<String> = flow {
        println("Start emitting")
        emit("a")
        println("Emitted a")
        emit("b")
        println("Emitted b")
        emit("c")
        println("Emitted c")
        emit("d")
        println("Emitted d")
        emit("e")
        println("Emitted e")
        emit("f")
        println("Emitted f")
    }.shareIn(this, SharingStarted.Lazily, 1)

    val mutableSharedFlow = MutableSharedFlow<String>()


    val first = launch {
        myFlow.collect {
            println(it)
            delay(100)
        }
    }
    val second = launch {
        delay(200)
        myFlow.collect {
            println(it)
            delay(100)
        }
    }

    first.join()
    second.join()
}

suspend fun sharedFlowDemo() = coroutineScope {
    val flow = MutableSharedFlow<Int>(extraBufferCapacity = 10)

    val first = launch { flow.collect { println("First $it") } }
    val second = launch { flow.collect { println("Second $it") } }
    val third = launch { flow.collect { println("Third $it") } }

    for (i in 1..10) {
        println("Emitting $i")
        flow.emit(i)
    }

    first.join()
    second.join()
    third.join()
}

suspend fun stateFlowDemo() = coroutineScope {
    val flow = MutableStateFlow(0)

    launch {
        for (i in 1..10) {
            flow.value = i
            delay(100)
        }
    }

    launch {
        flow.collect { println("New state (1): $it") }
    }

    launch {
        flow.collect { println("New state (2): $it") }
    }


    val job = launch {
        flow.collect { println("New state (3): $it") }
    }

    job.join()
}

private suspend fun demoCallbackFlow() = coroutineScope {
    val sendChannel = Channel<String>(BUFFERED)
    val flow = webSocketDemo(sendChannel)
    launch {
        flow.collect {
            println("Received $it")
        }
    }

    val job = launch {
        sendChannel.send("Hello")
        println("Send 'Hello'")
        sendChannel.send("World!")
        println("Send 'World'")
        delay(5000)
        sendChannel.send("CLOSE")
        println("Send 'CLOSE'")
    }

    job.join()
}


suspend fun webSocketDemo(outgoing: Channel<String>) = coroutineScope {
    callbackFlow {
        println("Try to connect to https://echo.websocket.org")
        val url = "https://echo.websocket.org/"
        val httpClient = OkHttpClient().newBuilder().build()
        val wsRequest = Request.Builder().url(url).build()
        val callback = object : WebSocketListener() {
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                println("Closing: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                println("Failure: $response")
                t.printStackTrace()
            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                println("onOpen")
                trySend("OPENED")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                println("onMessage: $text")
                trySend(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                println("onClosed")
                close()
            }
        }
        val webSocket = httpClient.newWebSocket(wsRequest, callback)
        println("Connecting...")

        launch {
            for (text in outgoing) {
                println("Outgoing message: $text")
                if (text == "CLOSE") {
                    webSocket.close(1000, "OK")
                    this@callbackFlow.close()
                } else {
                    val result = webSocket.send(text)
                    println("Send result: $result")
                }
            }
        }

        awaitClose {
            outgoing.close()
            webSocket.cancel()
        }
    }
}