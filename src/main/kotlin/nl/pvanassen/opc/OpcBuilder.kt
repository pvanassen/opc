package nl.pvanassen.opc

import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class OpcBuilder internal constructor(private val hostname: String, private val port: Int) {

    private val opcDevices: MutableList<OpcDeviceBuilder> = LinkedList()

    private var soTimeout = 5000
    private var soConnTimeout = 5000
    private var reuseAddress = false
    private var errorListeners: MutableList<Consumer<Exception>> = LinkedList()

    fun setSoTimeout(soTimeout: Int): OpcBuilder {
        this.soTimeout = soTimeout
        return this
    }

    fun setSoConTimeout(soConnTimeout: Int): OpcBuilder {
        this.soConnTimeout = soConnTimeout
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

    fun addDevice(): OpcDeviceBuilder {
        val opcDeviceBuilder = OpcDeviceBuilder(this)
        opcDevices.add(opcDeviceBuilder)
        return opcDeviceBuilder
    }

    class OpcDeviceBuilder(private val parent: OpcBuilder) {

        val pixelStrips: MutableList<PixelStripBuilder> = LinkedList()

        fun addPixelStrip(pixelCount: Int): OpcDeviceBuilder {
            val pixelStripBuilder = PixelStripBuilder(pixelCount)
            pixelStrips.add(pixelStripBuilder)
            return this
        }

        fun createDevice(): OpcBuilder {
            return parent
        }

        class PixelStripBuilder(val pixelCount: Int)
    }

    fun build(): Opc {
        val numberOfPixels = opcDevices.flatMap { it.pixelStrips }.sumOf { it.pixelCount }
        val opcSettings =
            OpcSettings(hostname, port, numberOfPixels, soTimeout, soConnTimeout, reuseAddress, errorListeners)

        val strip = AtomicInteger(0)
        val opcTree =
            OpcTree(opcDevices.flatMap { it.pixelStrips }.associate { Pair(strip.getAndIncrement(), it.pixelCount) })

        return Opc(opcSettings, opcTree)
    }

}