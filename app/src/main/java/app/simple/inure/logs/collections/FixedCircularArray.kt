package app.simple.inure.logs.collections

import kotlin.math.min

class FixedCircularArray<E>(val capacity: Int, initialSize: Int = INITIAL_SIZE) : Iterable<E> {

    companion object {
        private const val INITIAL_SIZE = 16
    }

    private var array = arrayOfNulls<Any>(min(capacity, initialSize))
    private var head = 0
    private var next = 0

    init {
        resetHead()
        if (capacity <= 0) {
            throw IllegalStateException("capacity (= $capacity) must be > 0")
        }
    }

    val size: Int
        get() {
            return when {
                head == -1 -> 0
                next <= head -> (capacity - head) + next
                else -> next - head
            }
        }

    fun add(list: Iterable<E>) {
        for (e in list) {
            add(e)
        }
    }

    fun add(e: E) {
        tryGrow()

        if (head < 0) {
            head = 0
        } else if (next == head) {
            head = ++head % capacity
        }

        array[next] = e
        next = ++next % capacity
    }

    fun remove(e: E): E? {
        if (e == null) {
            return null
        }

        val index = indexOf(e)
        if (index != -1) {
            return removeAt(index)
        }
        return null
    }

    @Suppress("unchecked_cast")
    fun removeAt(index: Int): E {
        checkIOBAndThrow(index)
        val result = array[(head + index) % capacity] as E
        for (i in index until (size - 1)) {
            array[(head + i) % capacity] = array[(head + i + 1) % capacity]
        }
        array[(head + size - 1) % capacity] = null

        next = (head + size - 1) % capacity
        if (next == head) {
            // empty array, reset
            resetHead()
        }

        return result
    }

    fun indexOf(e: E): Int {
        if (e == null) {
            return -1
        }

        for (i in 0 until size) {
            if (array[(head + i) % capacity] == e) {
                return i
            }
        }

        return -1
    }

    @Suppress("unchecked_cast")
    operator fun get(index: Int): E {
        checkIOBAndThrow(index)
        return array[(head + index) % capacity] as E
    }

    private fun checkIOBAndThrow(index: Int) {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("index = $index, size = $size")
        }
    }

    private fun tryGrow() {
        if (size < array.size || array.size == capacity) {
            return
        }

        val newSize = min(array.size * 2, capacity)
        val newArray = arrayOfNulls<Any>(newSize)
        System.arraycopy(array, 0, newArray, 0, array.size)
        array = newArray
    }

    operator fun plusAssign(e: E) = add(e)

    operator fun plusAssign(list: List<E>) = add(list)

    operator fun plusAssign(list: FixedCircularArray<E>) = add(list)

    fun clear() {
        resetHead()
        array.fill(null)
    }

    private fun resetHead() {
        head = -1
        next = 0
    }

    fun isEmpty() = size == 0

    fun isNotEmpty() = size != 0

    fun isFull() = size == capacity

    operator fun contains(element: E): Boolean {
        for (i in 0 until size) {
            if (element == array[(head + i) % capacity]) {
                return true
            }
        }
        return false
    }

    override fun iterator(): Iterator<E> = FixedCircularArrayIterator()

    private inner class FixedCircularArrayIterator : Iterator<E> {
        var index = 0

        override fun hasNext(): Boolean = index < size

        override fun next(): E = get(index++)
    }
}