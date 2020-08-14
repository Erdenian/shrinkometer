# shrinkometer

Gradle plugin that calculates fields, methods, classes and packages size difference before and after ProGuard or R8 shrinking.

## Usage

**This plugin is currently in the process of being approved by the Gradle Plugin Portal maintainers, so it cannot be applied to your project yet.**

Add classpath dependency to your root build.gradle file:
```kotlin
buildscript {
    dependencies {
        classpath("ru.erdenian:shrinkometer:0.1.0")
    }
}
```

And then add plugin to plugins block in build.gradle in your app module:
```kotlin
plugins {
    id("ru.erdenian.testplugin")
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