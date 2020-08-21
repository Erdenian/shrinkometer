package ru.erdenian.shrinkometer.core

import java.io.Reader

internal fun readAndCompare(debug: Reader, release: Reader) = debug.readStructure().apply {
    fillMinifiedSizes(release.readStructure())
}
