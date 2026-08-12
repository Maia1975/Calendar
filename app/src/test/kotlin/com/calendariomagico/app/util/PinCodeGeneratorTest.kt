package com.calendariomagico.app.util

import com.google.common.truth.Truth.assertThat
import kotlin.random.Random
import org.junit.Test

class PinCodeGeneratorTest {

    @Test
    fun `generate produces codes of the expected length`() {
        val code = PinCodeGenerator.generate(Random(42))
        assertThat(code).hasLength(PinCodeGenerator.LENGTH)
    }

    @Test
    fun `generate never includes ambiguous characters`() {
        repeat(200) { seed ->
            val code = PinCodeGenerator.generate(Random(seed))
            assertThat(code).containsNoneIn(listOf('0', 'O', '1', 'I', 'L'))
        }
    }

    @Test
    fun `generate is uppercase alphanumeric`() {
        val code = PinCodeGenerator.generate(Random(7))
        assertThat(code).matches("[A-Z0-9]+")
    }

    @Test
    fun `normalize strips dashes, spaces and lowercases become uppercase`() {
        assertThat(PinCodeGenerator.normalize(" g7k-2qm ")).isEqualTo("G7K2QM")
    }

    @Test
    fun `isValidFormat accepts a well formed code`() {
        val code = PinCodeGenerator.generate(Random(3))
        assertThat(PinCodeGenerator.isValidFormat(code)).isTrue()
    }

    @Test
    fun `isValidFormat rejects wrong length`() {
        assertThat(PinCodeGenerator.isValidFormat("AB12")).isFalse()
    }

    @Test
    fun `isValidFormat rejects ambiguous characters`() {
        assertThat(PinCodeGenerator.isValidFormat("O1IL5G")).isFalse()
    }

    @Test
    fun `generate with different seeds tends to produce different codes`() {
        val codes = (1..50).map { PinCodeGenerator.generate(Random(it)) }.toSet()
        assertThat(codes.size).isGreaterThan(40)
    }
}
