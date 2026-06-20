package com.example.roadguideapp.goldhunt.events

import java.util.concurrent.CopyOnWriteArrayList

/** Lightweight pub/sub for future treasures, quests, and achievements. */
internal class DiscoveryEventBus {

    private val listeners = CopyOnWriteArrayList<(DiscoveryEvent) -> Unit>()

    fun subscribe(listener: (DiscoveryEvent) -> Unit) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: (DiscoveryEvent) -> Unit) {
        listeners.remove(listener)
    }

    fun publish(event: DiscoveryEvent) {
        for (listener in listeners) {
            runCatching { listener(event) }
        }
    }
}
