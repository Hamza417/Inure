package app.simple.inure.util

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
}