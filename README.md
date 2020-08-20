# shrinkometer

[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/ru/erdenian/shrinkometer/ru.erdenian.shrinkometer.gradle.plugin/maven-metadata.xml.svg?label=Gradle%20Plugin%20Portal)](https://plugins.gradle.org/plugin/ru.erdenian.shrinkometer)

Gradle plugin that calculates fields, methods, classes and packages size difference before and after ProGuard or R8 shrinking.

## Usage

Apply plugin to your root build.gradle file:
```kotlin
plugins {
    id("ru.erdenian.shrinkometer") version "0.2.1" apply false
}
```

And then add plugin to plugins block in build.gradle in your app module:
```kotlin
plugins {
    id("ru.erdenian.shrinkometer")
}
```

Now you can call Gradle task to make report of saved size after shrinkage.
```shell script
./gradlew calculateShrunkSize
```
or if your application has flavors
```shell script
./gradlew calculate[flavor_name_capitalized]ShrunkSize
```

The report will be located in build/shrinkometer directory.
