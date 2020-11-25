# MockFit
[![](https://jitpack.io/v/javaherisaber/MockFit.svg)](https://jitpack.io/#javaherisaber/MockFit)

Kotlin library to mock http responses that fits into [retrofit](https://github.com/square/retrofit) from square

## Dependency
Top level build.gradle
```groovy
allprojects {
   repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

Module level build.gradle
```groovy
dependencies {
  implementation "com.github.javaherisaber.MockFit:runtime:$versions.mockFit"
  kapt "com.github.javaherisaber.MockFit:compiler:$versions.mockFit" // for Kotlin (make sure to include kapt plugin also)
  annotationProcessor "com.github.javaherisaber.MockFit:compiler:$versions.mockFit" // for Java
}
```

## Todo

- Add example usage and demo gif
- Use builder design pattern to simplify constructor
- Split codes to more classes
