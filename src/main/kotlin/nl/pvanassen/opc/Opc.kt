package nl.pvanassen.opc

import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.experimental.and
import kotlin.experimental.or

class Opc(private val settings: OpcSettings, private val opcTree: OpcTree):AutoCloseable{
    companion object {
        @JvmStatic
        fun builder(hostname: String, port: Int): OpcBuilder = OpcBuilder(hostname, port)
    }

    private val validateConnectionLock = ReentrantLock()

    private val socket = Socket()

    private val packetData: ByteArray

    private var output:OutputStream? = null
    private var firmwareConfig: Byte = 0

    private val address = InetSocketAddress(settings.hostname, settings.port)
    private val soConnTimeout = settings.soConnTimeout;

    init {
        val numberOfBytes = 3 * settings.numberOfPixels
        val packetLen = 4 + numberOfBytes
        this.packetData = ByteArray(packetLen)
        packetData[0] = 0
        this.packetData[1] = 0
        this.packetData[2] = (numberOfBytes shr 8).toByte()
        this.packetData[3] = (numberOfBytes and 255).toByte()

        this.socket.soTimeout = settings.soTimeout
        this.socket.reuseAddress = settings.reuseAddress
    }

    fun setColorCorrection(gamma:Float, red:Float, green:Float, blue:Float):Int {
        val content = "{ \"gamma\": $gamma, \"whitepoint\": [$red,$green,$blue]}".toByteArray()
        val packetLen = content.size + 4
        val header = byteArrayOf(0, -1, (packetLen shr 8).toByte(), (packetLen and 255).toByte(), 0, 1, 0, 1)
        ensureOpenConnection()
        if (!isConnectionOpen()) {
            return -1
        }
        if (writeData(header) == -1) {
            return -1
        }
        return writeData(content)
    }

    fun setDithering(enabled: Boolean) {
        firmwareConfig = if (enabled) {
            firmwareConfig and -2
        } else {
            firmwareConfig or 1
        }

        this.sendFirmwareConfigPacket()
    }

    fun setInterpolation(enabled: Boolean) {
        firmwareConfig = if (enabled) {
            firmwareConfig and -3
        } else {
            firmwareConfig or 2
        }

        this.sendFirmwareConfigPacket()
    }

    private fun sendFirmwareConfigPacket() {
        val packet = ByteArray(9)
        packet[0] = 0
        packet[1] = -1
        packet[2] = 0
        packet[3] = 5
        packet[4] = 0
        packet[5] = 1
        packet[6] = 0
        packet[7] = 2
        packet[8] = firmwareConfig
        writeData(packet)
    }

    private fun ensureOpenConnection() {
        if (this.output != null && socket.isConnected) {
            return
        }
        try {
            validateConnectionLock.withLock {
                this.socket.connect(address, soConnTimeout)
                this.socket.tcpNoDelay = true
                this.output = this.socket.getOutputStream()
            }
            this.sendFirmwareConfigPacket()
        }
        catch (e: Exception) {
            settings.errorListeners.forEach { it.accept(e) }
            close()
        }
    }

    private fun isConnectionOpen():Boolean = output != null && socket.isConnected

    private fun writeData(packetData: ByteArray): Int {
        if (packetData.isEmpty()) {
            return -1
        }
        ensureOpenConnection()
        if (!isConnectionOpen()) {
            return -1
        }
        return try {
            output!!.write(packetData)
            output!!.flush()
            0
        }
        catch (e: Exception) {
            settings.errorListeners.forEach { it.accept(e) }
            close()
            -1
        }
    }

    fun setPixelColor(opcPixel: Int, color: Int) {
        val offset = 4 + opcPixel * 3
        packetData[offset] = (color shr 16).toByte()
        packetData[offset + 1] = (color shr 8).toByte()
        packetData[offset + 2] = color.toByte()
    }

    fun setPixelColor(strip:Int, pixel: Int, color: Int) {
        setPixelColor(opcTree.getPixelNumber(strip, pixel), color)
    }

    fun flush(): Int {
        ensureOpenConnection()
        if (isConnectionOpen()) {
            return writeData(packetData)
        }
        return -1
    }


    fun clear() {
        (4 until packetData.size).forEach {
            packetData[it] = 0
        }
    }

    override fun close() {
        try {
            output?.close()
        } catch (e: Exception) {

        }
        finally {
            output = null
        }

        try {
            if (!socket.isClosed) {
                socket.close()
            }
        } catch (e: Exception) {

        }
    }

}