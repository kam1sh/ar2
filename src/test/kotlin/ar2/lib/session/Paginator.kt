package ar2.lib.session

class Paginator<T>(val callable: (offset: Int, size: Int) -> List<T>, var offset: Int = 0, val size: Int = 10) : Iterator<T> {
    lateinit var chunk: MutableList<T>
    var isLastChunk = false

    init {
        loadNext()
    }

    fun loadNext() {
        chunk = callable(offset, size).toMutableList()
        if (chunk.count() < size) { // we got less than expected
            isLastChunk = true
        }
    }

    override fun hasNext(): Boolean {
        return chunk.count() > 0 || !isLastChunk
    }

    override fun next(): T {
        if (chunk.count() == 0) loadNext()
        if (chunk.count() == 0) throw NoSuchElementException()
        val value = chunk.first()
        chunk.removeAt(0)
        return value
    }
}
