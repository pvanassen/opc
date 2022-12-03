package nl.pvanassen.opc

import java.util.function.Consumer

class OpcSettings(
    val hostname: String, val port: Int, val numberOfPixels: Int,
    val soTimeout: Int, val soConnTimeout: Int, val reuseAddress: Boolean,
    val errorListeners: List<Consumer<Exception>>
)