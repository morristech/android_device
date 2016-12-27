Modules
===============

Library is also distributed via **separate modules** which may be downloaded as standalone parts of
the library in order to decrease dependencies count in Android projects, so only dependencies really
needed in an Android project are included. **However** some modules may depend on another modules
from this library or on modules from other libraries.

Below are listed modules that are available for download also with theirs dependencies.

## Download ##

### Gradle ###

For **successful resolving** of artifacts for separate modules via **Gradle** add the following snippet
into **build.gradle** script of your desired Android project and use `compile '...'` declaration
as usually.

    repositories {
        maven {
            url  "http://dl.bintray.com/universum-studios/android"
        }
    }

**[Core](https://github.com/universum-studios/android_device/tree/master/library/src/main)**

    compile 'universum.studios.android:device-core:1.0.0@aar'

**[Battery](https://github.com/universum-studios/android_device/tree/master/library/src/battery)**

    compile 'universum.studios.android:device-battery:1.0.0@aar'

_depends on:_
[device-core](https://github.com/universum-studios/android_device/tree/master/library/src/main)

**[Connection](https://github.com/universum-studios/android_device/tree/master/library/src/connection)**

    compile 'universum.studios.android:device-connection:1.0.0@aar'

**[Connection-Core](https://github.com/universum-studios/android_device/tree/master/library/src/connection/core)**

    compile 'universum.studios.android:device-connection-core:1.0.0@aar'

**[Connection-Util](https://github.com/universum-studios/android_device/tree/master/library/src/connection/util)**

    compile 'universum.studios.android:device-connection-util:1.0.0@aar'

**[Screen](https://github.com/universum-studios/android_device/tree/master/library/src/screen)**

    compile 'universum.studios.android:device-screen:1.0.0@aar'

_depends on:_
[device-core](https://github.com/universum-studios/android_device/tree/master/library/src/main)

**[Screen-Core](https://github.com/universum-studios/android_device/tree/master/library/src/screen/core)**

    compile 'universum.studios.android:device-screen-core:1.0.0@aar'

_depends on:_
[device-core](https://github.com/universum-studios/android_device/tree/master/library/src/main)

**[Screen-Util](https://github.com/universum-studios/android_device/tree/master/library/src/screen/util)**

    compile 'universum.studios.android:device-screen-util:1.0.0@aar'

**[Storage](https://github.com/universum-studios/android_device/tree/master/library/src/storage)**

    compile 'universum.studios.android:device-storage:1.0.0@aar'

**[Storage-Core](https://github.com/universum-studios/android_device/tree/master/library/src/storage/core)**

    compile 'universum.studios.android:device-storage-core:1.0.0@aar'

_depends on:_
[device-core](https://github.com/universum-studios/android_device/tree/master/library/src/main)

**[Storage-Util](https://github.com/universum-studios/android_device/tree/master/library/src/storage/util)**

    compile 'universum.studios.android:device-storage-util:1.0.0@aar'

**[Util](https://github.com/universum-studios/android_device/tree/master/library/src/util)**

    compile 'universum.studios.android:device-util:1.0.0@aar'

_depends on:_
[device-screen-core](https://github.com/universum-studios/android_device/tree/master/library/src/screen/core)
