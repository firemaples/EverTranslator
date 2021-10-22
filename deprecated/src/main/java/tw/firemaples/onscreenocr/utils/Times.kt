package tw.firemaples.onscreenocr.utils

open class Times<T>(private var value: T, private val default: T, private var times: Int) {
    constructor(default: T, times: Int) : this(default, default, times)

    private var remain: Int = times

    fun setValue(value: T) {
        this.value = value
        resetTimes()
    }

    private fun resetTimes(times: Int = this.times) {
        this.times = times
        this.remain = times
    }

    fun whenValue(compareValue: T, action: () -> Unit) {
        if (isValue(compareValue)) action()
    }

    fun whenNotValue(compareValue: T, action: () -> Unit) {
        if (isNotValue(compareValue)) action()
    }

    public fun isValue(compareValue: T): Boolean {
        val result = value == compareValue
        countdown()
        return result
    }

    public fun isNotValue(compareValue: T): Boolean = !isValue(compareValue)

    public fun countdown() {
        if (--remain <= 0) value = default
    }
}

class Once<T>(default: T) : Times<T>(default, 1)