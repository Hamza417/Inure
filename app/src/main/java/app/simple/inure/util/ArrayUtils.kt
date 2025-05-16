package app.simple.inure.util

import org.json.JSONArray

object ArrayUtils {
    fun <E> ArrayList<E>.move(fromIndex: Int, toIndex: Int) {
        if (fromIndex >= size || fromIndex < 0) {
            throw IndexOutOfBoundsException(outOfBoundsMsg(fromIndex, size))
        }
        if (toIndex >= size || toIndex < 0) {
            throw IndexOutOfBoundsException(outOfBoundsMsg(toIndex, size))
        }
        if (fromIndex == toIndex) return
        var index = toIndex
        var item = get(index)
        if (fromIndex > toIndex) {
            while (index < fromIndex) {
                index++
                item = set(index, item)
            }
        } else {
            while (index > fromIndex) {
                index--
                item = set(index, item)
            }
        }
        set(toIndex, item)
    }

    private fun outOfBoundsMsg(index: Int, size: Int): String {
        return "Index: $index, Size: $size"
    }

    fun <T> List<T>.copyOf(): List<T> {
        return mutableListOf<T>().also { it.addAll(this) }
    }

    fun <T> List<T>.mutableCopyOf(): MutableList<T> {
        return mutableListOf<T>().also { it.addAll(this) }
    }

    fun cloned(arrayList: ArrayList<Any>): ArrayList<Any> {
        return arrayList.map {
            when (it) {
                is ArrayList<*> -> cloned(it.toList() as ArrayList<Any>)
                else -> it
            }
        } as ArrayList<Any>
    }

    /**
     * Deep copy an ArrayList items?
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> deepCopy(arrayList: ArrayList<T>): ArrayList<T> {
        return arrayList.map {
            when (it) {
                is ArrayList<*> -> deepCopy(it.toList() as ArrayList<T>)
                else -> it
            }
        } as ArrayList<T>
    }

    fun <T> List<T>.toArrayList(): ArrayList<T> {
        return this as ArrayList<T>
    }

    fun <T> List<T>.clone(): ArrayList<T> {
        @Suppress("UNCHECKED_CAST")
        return this.toArrayList().clone() as ArrayList<T>
    }

    /**
     * Split an [ArrayList] into multiple [ArrayList]s
     * @param count number of [ArrayList]s to split into
     */
    fun <T> ArrayList<T>.split(count: Int): ArrayList<ArrayList<T>> {
        val result = ArrayList<ArrayList<T>>()
        var remainder = size % count
        val size = size / count
        var index = 0
        for (i in 0 until count) {
            val list = ArrayList<T>()
            for (j in 0 until size) {
                list.add(this[index])
                index++
            }
            if (remainder > 0) {
                list.add(this[index])
                index++
                remainder--
            }
            result.add(list)
        }
        return result
    }

    fun JSONArray.toStringArray(): Array<String> {
        val list = ArrayList<String>()
        for (i in 0 until length()) {
            list.add(getString(i))
        }
        return list.toTypedArray()
    }

    fun <T> ArrayList<T>.addIfNotExists(element: T?, comparator: (T?, T?) -> Boolean) {
        synchronized(this) {
            if (any { comparator(it, element) }.not()) {
                element?.let {
                    add(it)
                }
            }
        }
    }

    fun <T> ArrayList<T>.removeIfExists(element: T, comparator: (T, T) -> Boolean) {
        synchronized(this) {
            if (any { comparator(it, element) }) {
                remove(element)
            }
        }
    }

    fun <T> ArrayList<T>.getMatchedCount(keyword: String, fieldExtractor: (T) -> String): Int {
        return this.filter {
            fieldExtractor(it).contains(keyword, ignoreCase = true)
        }.size
    }

    fun <T> Array<T?>.getMatchedCount(keyword: String, ignoreCase: Boolean, fieldExtractor: (T?) -> String): Int {
        return this.filter {
            it != null && fieldExtractor(it).contains(keyword, ignoreCase = ignoreCase)
        }.size
    }

    /**
     * Get second item from the list
     */
    fun <T> List<T>.second(): T {
        return this[1]
    }

    fun <T> List<T>?.circularGet(index: Int): T? {
        if (this.isNullOrEmpty()) return null
        return this[index % size]
    }
}
