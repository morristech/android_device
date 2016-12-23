Modules
===============

Library is also distributed via **separate modules** which may be downloaded as standalone parts of
the library in order to decrease dependencies count in Android projects, so only dependencies really
need in an Android project are included. **However** some modules may depend on another modules from
this library or on modules from other libraries.

Below are listed modules that are available for download also with theirs dependencies.

## Download ##

### Gradle ###

**[Core](https://github.com/universum-studios/android_preferences/tree/master/library/src/main)**

    compile 'universum.studios.android:preferences-core:1.0.0@aar'

**[Common](https://github.com/universum-studios/android_preferences/tree/master/library/src/common)**

    compile 'universum.studios.android:preferences-common:1.0.0@aar'

_depends on:_
[preferences-core](https://github.com/universum-studios/android_preferences/tree/master/library/src/main)

**[Collection](https://github.com/universum-studios/android_preferences/tree/master/library/src/collection)**

    compile 'universum.studios.android:preferences-collection:1.0.0@aar'

_depends on:_
[preferences-core](https://github.com/universum-studios/android_preferences/tree/master/library/src/main)
