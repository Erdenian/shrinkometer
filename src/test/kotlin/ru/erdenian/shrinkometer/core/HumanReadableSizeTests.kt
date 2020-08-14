package ru.erdenian.shrinkometer.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HumanReadableSizeTests {

    @Test
    fun bytes() {
        assertEquals("-1023 B", humanReadableSize(-1023L))
        assertEquals("-1 B", humanReadableSize(-1L))
        assertEquals("0 B", humanReadableSize(0L))
        assertEquals("1 B", humanReadableSize(1L))
        assertEquals("1023 B", humanReadableSize(1023L))
    }

    @Test
    fun kibi() {
        assertEquals("1,0 KiB", humanReadableSize(1024L))
        assertEquals("1,0 KiB", humanReadableSize(1024L + 1L))
        assertEquals("1,3 KiB", humanReadableSize(1324L))
        assertEquals("1023,0 KiB", humanReadableSize(1023L * 1024L))
    }

    @Test
    fun mebi() {
        assertEquals("1,0 MiB", humanReadableSize(1024L * 1024L))
        assertEquals("1,0 MiB", humanReadableSize(1024L * 1024L + 1))
        assertEquals("1,3 MiB", humanReadableSize(1324L * 1024L))
        assertEquals("1023,0 MiB", humanReadableSize(1023L * 1024L * 1024L))
    }

    @Test
    fun gibi() {
        assertEquals("1,0 GiB", humanReadableSize(1024L * 1024L * 1024L))
        assertEquals("1,0 GiB", humanReadableSize(1024L * 1024L * 1024L + 1))
        assertEquals("1,3 GiB", humanReadableSize(1324L * 1024L * 1024L))
        assertEquals("1023,0 GiB", humanReadableSize(1023L * 1024 * 1024L * 1024L))
    }

    @Test
    fun tebi() {
        assertEquals("1,0 TiB", humanReadableSize(1024L * 1024 * 1024L * 1024L))
        assertEquals("1,0 TiB", humanReadableSize(1024L * 1024 * 1024L * 1024L + 1))
        assertEquals("1,3 TiB", humanReadableSize(1324L * 1024 * 1024L * 1024L))
        assertEquals("1023,0 TiB", humanReadableSize(1023L * 1024 * 1024 * 1024L * 1024L))
    }

    @Test
    fun pebi() {
        assertEquals("1,0 PiB", humanReadableSize(1024L * 1024 * 1024 * 1024L * 1024L))
        assertEquals("1,0 PiB", humanReadableSize(1024L * 1024 * 1024 * 1024L * 1024L + 1))
        assertEquals("1,3 PiB", humanReadableSize(1324L * 1024 * 1024 * 1024L * 1024L))
        assertEquals("1023,0 PiB", humanReadableSize(1023L * 1024 * 1024 * 1024 * 1024L * 1024L))
    }

    @Test
    fun exbi() {
        assertEquals("1,0 EiB", humanReadableSize(1024L * 1024 * 1024 * 1024 * 1024L * 1024L))
        assertEquals("1,0 EiB", humanReadableSize(1024L * 1024 * 1024 * 1024 * 1024L * 1024L + 1))
        assertEquals("1,3 EiB", humanReadableSize(1324L * 1024 * 1024 * 1024 * 1024L * 1024L))
    }
}
