package nl.pvanassen.opc

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class LedModelTest {

    @Test
    fun testMapOneStrip() {
        val tree = LedModel(mapOf(Pair(0, 10)))
        assertThat(tree).isNotNull
        assertThat(tree.getPixelNumber(0, 5)).isEqualTo(5)
        assertThat(tree.totalPixels).isEqualTo(10)
    }

    @Test
    fun testOutOfBoundsOneStrip() {
        val tree = LedModel(mapOf(Pair(0, 10)))
        assertThat(tree).isNotNull
        assertThatThrownBy {
            tree.getPixelNumber(0, 10)
        }.isInstanceOf(IllegalArgumentException::class.java)

    }

    @Test
    fun testOutOfBoundsMultipleStripsSameSize() {
        val tree = LedModel(mapOf(Pair(0, 10), Pair(1, 10)))
        assertThat(tree).isNotNull
        assertThat(tree.getPixelNumber(0, 9)).isEqualTo(9)
        assertThat(tree.getPixelNumber(1, 0)).isEqualTo(10)
        assertThat(tree.getPixelNumber(1, 9)).isEqualTo(19)
        assertThatThrownBy {
            tree.getPixelNumber(0, 10)
        }.isInstanceOf(IllegalArgumentException::class.java)
        assertThat(tree.totalPixels).isEqualTo(20)
    }

    @Test
    fun testMapMultipleStripsSameSize() {
        val tree = LedModel(mapOf(Pair(0, 10), Pair(1, 10), Pair(2, 10)))
        assertThat(tree).isNotNull
        assertThat(tree.getPixelNumber(0, 5)).isEqualTo(5)
        assertThat(tree.getPixelNumber(1, 5)).isEqualTo(15)
        assertThat(tree.getPixelNumber(2, 5)).isEqualTo(25)
        assertThat(tree.totalPixels).isEqualTo(30)
    }

    @Test
    fun testMapMultipleStripsDifferentSizes() {
        val tree = LedModel(mapOf(Pair(0, 15), Pair(1, 20), Pair(2, 3)))
        assertThat(tree).isNotNull
        assertThat(tree.getPixelNumber(0, 5)).isEqualTo(5)
        assertThat(tree.getPixelNumber(1, 1)).isEqualTo(16)
        assertThat(tree.getPixelNumber(2, 2)).isEqualTo(37)
        assertThat(tree.totalPixels).isEqualTo(38)
    }
}