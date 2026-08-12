package com.calendariomagico.app.util

import kotlin.random.Random

/**
 * Generates short invite codes. Ambiguous characters (0/O, 1/I/L) are excluded
 * so the code is easy to read aloud and retype on a small phone keyboard.
 */
object PinCodeGenerator {
    private const val ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
    const val LENGTH = 6

    fun generate(random: Random = Random.Default): String =
        (1..LENGTH)
            .map { ALPHABET[random.nextInt(ALPHABET.length)] }
            .joinToString(separator = "")

    fun normalize(input: String): String =
        input.trim().uppercase().replace("-", "").replace(" ", "")

    fun isValidFormat(input: String): Boolean {
        val normalized = normalize(input)
        return normalized.length == LENGTH && normalized.all { it in ALPHABET }
    }
}
