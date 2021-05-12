package ru.erdenian.shrinkometer.core

import java.io.Reader
import ru.erdenian.shrinkometer.core.readers.fillMinifiedSizes
import ru.erdenian.shrinkometer.core.readers.readStructure

internal fun readAndCompare(debug: Reader, release: Reader) = debug.readStructure().apply {
    fillMinifiedSizes(release.readStructure())
}
