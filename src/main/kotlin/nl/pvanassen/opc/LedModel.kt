package nl.pvanassen.opc

class LedModel(private val tree: Map<Int, Int>) {

    private val pixelNumbers: Map<Int, Int>

    val totalPixels: Int

    init {
        totalPixels = tree.values.sum()
        val pixelNumbers: MutableMap<Int, Int> = HashMap()
        tree.entries.indices.forEach { pos ->
            pixelNumbers[pos] = if (pos == 0) {
                0
            } else {
                pixelNumbers[pos - 1]!! + tree.values.elementAt(pos - 1)
            }
        }
        this.pixelNumbers = pixelNumbers.toMap()
    }

    fun getPixelNumber(strip: Int, pixel: Int): Int {
        if (pixel > tree[strip]!! - 1) {
            throw IllegalArgumentException("Requesting out of bound pixel")
        }
        return pixelNumbers[strip]!! + pixel
    }

}