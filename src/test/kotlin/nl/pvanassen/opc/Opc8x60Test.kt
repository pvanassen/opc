package nl.pvanassen.opc

import fr.bmartel.opc.OpcClient
import fr.bmartel.opc.PixelStrip
import org.junit.Ignore
import org.junit.Test
import java.lang.System.out
import java.util.*

class Opc8x60Test {

    @Test
    @Ignore("Enable for local testing")
    fun test8x60() {
        val opc = Opc.builder(System.getenv().get("fadecandy-server")!!, 7890)
                .addDevice()
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                .createDevice()
                .addDevice()
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                    .addPixelStrip(20)
                .createDevice()
            .build()
        opc.clear()
        opc.setColorCorrection(1.8f, .2f, .2f, .2f)
        opc.flush()

        (0 until 8).forEach {strip ->
            (0 until strip + 1).forEach {
                out.println("Strip: $strip, pixel: ${2 + it}")
                opc.setPixelColor(strip, 2 + it, 255)
            }
        }
        (8 until 16).forEach {strip ->
            (0 until strip + 1 - 8).forEach {
                out.println("Strip: $strip, pixel: ${2 + it}")
                opc.setPixelColor(strip, 2 + it, 255 shl 8)
            }
        }
        opc.flush()
    }


    @Test
    @Ignore("Enable for local testing")
    fun testOldOpc8x60() {
        val pixelStrips:MutableList<PixelStrip> = LinkedList()

        val opc = OpcClient(System.getenv().get("fadecandy-server")!!, 7890)
        val device1 = opc.addDevice()
        pixelStrips.add(device1!!.addPixelStrip(0, 20))
        pixelStrips.add(device1.addPixelStrip(1, 20))
        pixelStrips.add(device1.addPixelStrip(2, 20))
        pixelStrips.add(device1.addPixelStrip(3, 20))
        pixelStrips.add(device1.addPixelStrip(4, 20))
        pixelStrips.add(device1.addPixelStrip(5, 20))
        pixelStrips.add(device1.addPixelStrip(6, 20))
        pixelStrips.add(device1.addPixelStrip(7, 20))
        val device2 = opc.addDevice()
        pixelStrips.add(device2!!.addPixelStrip(0, 20))
        pixelStrips.add(device2.addPixelStrip(1, 20))
        pixelStrips.add(device2.addPixelStrip(2, 20))
        pixelStrips.add(device2.addPixelStrip(3, 20))
        pixelStrips.add(device2.addPixelStrip(4, 20))
        pixelStrips.add(device2.addPixelStrip(5, 20))
        pixelStrips.add(device2.addPixelStrip(6, 20))
        pixelStrips.add(device2.addPixelStrip(7, 20))

        opc.addSocketListener { exception -> System.err.println("Exception caught"); exception.printStackTrace() }

        pixelStrips[2].setPixelColor(6, 255)
        pixelStrips[9].setPixelColor(6, 255)
        opc.show()

//        opc.setColorCorrection(2.5f, 1f, 1f, 1f)
//        (0 until 8).forEach {strip ->
//            (0 until strip + 1).forEach {
//                out.println("Strip: $strip, pixel: ${30 + it}")
//                pixelStrips[strip].setPixelColor(30 + it, 255)
//            }
//        }
        opc.show()
    }



}