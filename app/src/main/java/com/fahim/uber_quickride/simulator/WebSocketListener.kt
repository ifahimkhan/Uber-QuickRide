package com.fahim.uber_quickride.simulator

interface WebSocketListener {

    fun onConnect()

    fun onDisconnect()

    fun onError(error: String)

    fun onMessage(data: String)
}