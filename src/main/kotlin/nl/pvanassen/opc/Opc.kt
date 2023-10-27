package nl.pvanassen.opc

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlin.experimental.and
import kotlin.experimental.or

class Opc(private val settings: OpcSettings, val ledModel: LedModel) : AutoCloseable {
    companion object {
        @JvmStatic
        fun builder(hostname: String, port: Int): OpcBuilder = OpcBuilder(hostname, port)
    }

    private val packetData: ByteArray

    private val address = InetSocketAddress(settings.hostname, settings.port)

    private var socket: Socket? = null
    private var channel: ByteWriteChannel? = null
    private var firmwareConfig: Byte = 0

    init {
        val numberOfBytes = 3 * settings.numberOfPixels
        val packetLen = 4 + numberOfBytes
        this.packetData = ByteArray(packetLen)
        packetData[0] = 0   // Channel 0
        this.packetData[1] = 0 // Command (set pixel)
        this.packetData[2] = (numberOfBytes shr 8).toByte()
        this.packetData[3] = (numberOfBytes and 255).toByte()
    }

    suspend fun setColorCorrection(gamma: Float, red: Float, green: Float, blue: Float): Int {
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

    suspend fun setDithering(enabled: Boolean) {
        firmwareConfig = if (enabled) {
            firmwareConfig and -2
        } else {
            firmwareConfig or 1
        }

        this.sendFirmwareConfigPacket()
    }

    suspend fun setInterpolation(enabled: Boolean) {
        firmwareConfig = if (enabled) {
            firmwareConfig and -3
        } else {
            firmwareConfig or 2
        }

        this.sendFirmwareConfigPacket()
    }

    private suspend fun sendFirmwareConfigPacket() {
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

    private suspend fun ensureOpenConnection() {
        if (isConnectionOpen()) {
            return
        }
        try {
            socket = aSocket(SelectorManager(Dispatchers.IO))
                .tcp()
                .connect(address) {
                    socketTimeout = settings.soTimeout
                    reuseAddress = settings.reuseAddress
                    noDelay = true
                }
            channel = socket!!.openWriteChannel(autoFlush = false)
            this.sendFirmwareConfigPacket()
        } catch (e: Exception) {
            settings.errorListeners.forEach { it.accept(e) }
        }
    }

    private fun isConnectionOpen(): Boolean = channel != null

    private suspend fun writeData(packetData: ByteArray): Int {
        if (packetData.isEmpty()) {
            return -1
        }
        ensureOpenConnection()
        if (!isConnectionOpen()) {
            return -1
        }
        return try {
            channel!!.writeFully(packetData)
            channel!!.flush()
            0
        } catch (e: Exception) {
            settings.errorListeners.forEach { it.accept(e) }
            close()
            -1
        }
    }

    fun setPixelColor(pixel: Int, color: Int) {
        val offset = 4 + pixel * 3
        packetData[offset] = (color shr 16).toByte()
        packetData[offset + 1] = (color shr 8).toByte()
        packetData[offset + 2] = color.toByte()
    }

    fun setPixelColor(strip: Int, pixel: Int, color: Int) {
        setPixelColor(ledModel.getPixelNumber(strip, pixel), color)
    }

    suspend fun flush(): Int {
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
            channel?.close()
        } catch (e: Exception) {

        } finally {
            channel = null
        }
        try {
            socket?.close()
        } catch (e: Exception) {

        } finally {
            socket = null
        }
    }

}