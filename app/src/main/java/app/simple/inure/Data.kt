package app.simple.inure

fun main() {
    val arr = intArrayOf(6, 33, 24, 22, 9, 45, 21, 36, 3, 29,
                         25, 7, 12, 25, 40, 35, 38, 16, 43, 10,
                         29, 17, 49, 38, 27, 19, 34, 26, 32, 11,
                         46, 27, 22, 24, 31, 28, 39, 23, 31, 37,
                         31, 27, 26, 30, 15, 20, 30, 36, 21, 13)

    arr.sort()

    var x = 0

    for (i in arr) {
        if (i > 40) {
            x++
        }
    }

    println(x)
}