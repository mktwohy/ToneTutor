package com.example.tonetuner_v2.extensions

import java.util.concurrent.BlockingQueue

/** Similar to offer(), but, if there is no space, it removes an element to make room */
fun <T> BlockingQueue<T>.forcedOffer(element: T) {
    if (remainingCapacity() == 0) poll()
    offer(element)
}

fun <T> BlockingQueue<T>.clearAndOffer(element: T) {
    clear()
    offer(element)
}