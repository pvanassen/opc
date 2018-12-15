package nl.pvanassen.opc

import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class OpcBuilder internal constructor(private val hostname:String, private val port:Int) {

    private val opcDevices:MutableList<OpcDeviceBuilder> = LinkedList()

    private var soTimeout = 5000
    private var soConnTimeout = 5000
    private var reuseAddress = false
    private var errorListeners:MutableList<Consumer<Exception>> = LinkedList()

    fun setSoTimeout(soTimeout: Int) {
        this.soTimeout = soTimeout
    }

    fun setSoConTimeout(soConnTimeout: Int) {
        this.soConnTimeout = soConnTimeout
    }

    fun addErrorListener(listener: Consumer<Exception>) {
        this.errorListeners.add(listener)
    }

    fun setReuseAddress(reuseAddress: Boolean) {
        this.reuseAddress = reuseAddress
    }

    fun addDevice():OpcDeviceBuilder {
        val opcDeviceBuilder = OpcDeviceBuilder(this)
        opcDevices.add(opcDeviceBuilder)
        return opcDeviceBuilder
    }

    class OpcDeviceBuilder(private val parent: OpcBuilder) {

        val pixelStrips:MutableList<PixelStripBuilder> = LinkedList()

        fun addPixelStrip(pixelCount:Int):OpcDeviceBuilder {
            val pixelStripBuilder = PixelStripBuilder(pixelCount)
            pixelStrips.add(pixelStripBuilder)
            return this
        }

        fun createDevice(): OpcBuilder {
            return parent
        }

        class PixelStripBuilder(val pixelCount:Int)
    }

    fun build(): Opc {
        /*
        val hostname:String, val port:Int, val numberOfPixels:Int,
                  val soTimeout:Int,  val soConnTimeout:Int, val reuseAddress:Boolean,
                  val errorListeners:List<Consumer<Exception>>
         */
        val numberOfPixels = opcDevices.flatMap { it.pixelStrips }.map { it.pixelCount }.sum()
        val opcSettings = OpcSettings(hostname, port, numberOfPixels, soTimeout, soConnTimeout, reuseAddress, errorListeners)

        val strip = AtomicInteger(0)
        val opcTree = OpcTree(opcDevices.flatMap { it.pixelStrips }.map { Pair(strip.getAndIncrement(), it.pixelCount) }.toMap())

        return Opc(opcSettings, opcTree)
    }

}