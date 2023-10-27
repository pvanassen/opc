package nl.pvanassen.opc

import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class OpcBuilder internal constructor(private val hostname: String, private val port: Int) {

    private var soTimeout = 5000L
    private var reuseAddress = false
    private val errorListeners: MutableList<Consumer<Exception>> = LinkedList()

    private val pixelStrips: MutableList<PixelStripBuilder> = LinkedList()

    fun addPixelStrip(pixelCount: Int): OpcBuilder {
        val pixelStripBuilder = PixelStripBuilder(pixelCount)
        pixelStrips.add(pixelStripBuilder)
        return this
    }

    fun setSoTimeout(soTimeout: Long): OpcBuilder {
        this.soTimeout = soTimeout
        return this
    }

    fun addErrorListener(listener: Consumer<Exception>): OpcBuilder {
        this.errorListeners.add(listener)
        return this
    }

    fun setReuseAddress(reuseAddress: Boolean): OpcBuilder {
        this.reuseAddress = reuseAddress
        return this
    }

    fun build(): Opc {
        val numberOfPixels = pixelStrips.sumOf { it.pixelCount }
        val opcSettings =
            OpcSettings(hostname, port, numberOfPixels, soTimeout, reuseAddress, errorListeners)

        val strip = AtomicInteger(0)
        val ledModel =
            LedModel(pixelStrips.associate { Pair(strip.getAndIncrement(), it.pixelCount) })

        return Opc(opcSettings, ledModel)
    }

    private class PixelStripBuilder(val pixelCount: Int)

}