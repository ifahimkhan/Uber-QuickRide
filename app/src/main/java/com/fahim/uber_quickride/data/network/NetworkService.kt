package com.fahim.uber_quickride.data.network

import com.fahim.uber_quickride.simulator.WebSocket
import com.fahim.uber_quickride.simulator.WebSocketListener

class NetworkService {

    fun createWebSocket(webSocketListener: WebSocketListener): WebSocket {
        return WebSocket(webSocketListener)
    }

}