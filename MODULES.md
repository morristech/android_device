Modules
===============

Library is also distributed via **separate modules** which may be downloaded as standalone parts of
the library in order to decrease dependencies count in Android projects, so only dependencies really
needed in an Android project are included. **However** some modules may depend on another modules
from this library or on modules from other libraries.

## Download ##

### Gradle ###

For **successful resolving** of artifacts for separate modules via **Gradle** add the following snippet
into **build.gradle** script of your desired Android project and use `implementation '...'` declaration
as usually.

    repositories {
        maven {
            url  "http://dl.bintray.com/universum-studios/android"
        }
    }


## Available modules ##
> Following modules are available in the [latest](https://github.com/universum-studios/android_device/releases "Releases page") stable release.

- **[Core](https://github.com/universum-studios/android_device/tree/master/library-core)**
- **[Battery](https://github.com/universum-studios/android_device/tree/master/library-battery)**
- **[@Connection](https://github.com/universum-studios/android_device/tree/master/library-connection_group)**
- **[Connection-Core](https://github.com/universum-studios/android_device/tree/master/library-connection-core)**
- **[Connection-Util](https://github.com/universum-studios/android_device/tree/master/library-connection-util)**
- **[@Screen](https://github.com/universum-studios/android_device/tree/master/library-screen_group)**
- **[Screen-Core](https://github.com/universum-studios/android_device/tree/master/library-screen-core)**
- **[Screen-Util](https://github.com/universum-studios/android_device/tree/master/library-screen-util)**
- **[@Storage](https://github.com/universum-studios/android_device/tree/master/library-storage_group)**
- **[Storage-Core](https://github.com/universum-studios/android_device/tree/master/library-storage-core)**
- **[Storage-Util](https://github.com/universum-studios/android_device/tree/master/library-storage-util)**
- **[Util](https://github.com/universum-studios/android_device/tree/master/library-util)**
