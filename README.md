# shrinkometer

[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/ru/erdenian/shrinkometer/ru.erdenian.shrinkometer.gradle.plugin/maven-metadata.xml.svg?label=Gradle%20Plugin%20Portal)](https://plugins.gradle.org/plugin/ru.erdenian.shrinkometer)

Gradle plugin that calculates fields, methods, classes and packages size difference before and after ProGuard or R8 shrinking.

## Requirements

* Android Gradle plugin 3.0.0 or higher.
* Gradle 4.1 or higher (required by AGP 3.0.0).

## Usage

Add plugin to your root build.gradle file:
```kotlin
plugins {
    id("ru.erdenian.shrinkometer") version "[version]" apply false
}
```

And then apply plugin in build.gradle file in your app module:
```kotlin
plugins {
    id("ru.erdenian.shrinkometer")
}
```

Now you can call Gradle task to make report of saved size after shrinkage:
```shell script
./gradlew shrinkometer
```
or if your application has flavors:
```shell script
./gradlew shrinkometer[flavor_name_capitalized]
```

The report will be located in build/shrinkometer directory.
