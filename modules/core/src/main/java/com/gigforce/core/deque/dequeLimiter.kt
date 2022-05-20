package com.gigforce.core.deque

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


fun <E> dequeLimiter(limit: Int): ReadWriteProperty<Any?, ArrayDeque<E>> =
  object : ReadWriteProperty<Any?, ArrayDeque<E>> {

    private var deque: ArrayDeque<E> = ArrayDeque(limit)

    private fun applyLimit() {
      while (deque.size > limit) {
        val removed = deque.removeFirst()
        println("dequeLimiter removed $removed")
      }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): ArrayDeque<E> {
      applyLimit()
      return deque
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: ArrayDeque<E>) {
      this.deque = value
      applyLimit()
    }
  }