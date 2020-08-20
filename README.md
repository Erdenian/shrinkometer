# shrinkometer

Gradle plugin that calculates fields, methods, classes and packages size difference before and after ProGuard or R8 shrinking.

## Usage

Add classpath dependency to your root build.gradle file:
```kotlin
buildscript {
    dependencies {
        classpath("ru.erdenian:shrinkometer:0.2.0")
    }
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
