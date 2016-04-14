Gradle Configuration Resolver Plugin
====================================

This plugin adds a task that will resolve all dependencies in a particular project, and add
information about the dependencies into a list named `allDeps` in the project object.

This functionality may be useful for ensuring that all dependencies have been resolved into the
local Gradle cache. The `allDeps` list can also be used to programmatically process dependencies
elsewhere in the buildscript.

Usage
-----

Apply the plugin using either the buildscript closure and an `apply plugin`:

```Gradle
buildscript {
    repositories {
        maven {
            url 'https://plugins.gradle.org/m2/'
        }
    }
    dependencies {
        classpath 'gradle.plugin.com.palantir.configurationresolver:gradle-configuration-resolver-plugin:0.1.0'
    }
}

apply plugin: 'com.palantir.configuration-resolver'
```

Or use the plugins closure:

```Gradle
plugins {
    id 'com.palantir.configuration-resolver' version '0.1.0'
}
```

This will add the `resolveConfigurations` task to the project.

License
-------
This project is made available under the [Apache 2.0 License][license].


[license]: http://www.apache.org/licenses/LICENSE-2.0
