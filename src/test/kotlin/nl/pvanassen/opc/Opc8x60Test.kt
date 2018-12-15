package nl.pvanassen.opc

import org.junit.Test
import java.lang.System.out

class Opc8x60Test {

    @Test
    fun test8x60() {
        val opc = Opc.builder(System.getenv().get("fadecandy-server")!!, 7890)
                .addDevice()
                    .addPixelStrip(60)
                    .addPixelStrip(60)
                    .addPixelStrip(60)
                    .addPixelStrip(60)
                    .addPixelStrip(60)
                    .addPixelStrip(60)
                    .addPixelStrip(60)
                    .addPixelStrip(60)
                .createDevice()
            .build()
        opc.clear()
        opc.flush()
        opc.setColorCorrection(2.5f, 0.2f, 0.2f, 0.2f)
        (0 until 8).forEach {strip ->
            (0 until strip + 1).forEach {
                out.println("Strip: $strip, pixel: ${30 + it}")
                opc.setPixelColor(strip, 30 + it, 255)
            }
        }
        opc.flush()
    }
}