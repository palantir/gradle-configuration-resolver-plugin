Gradle Configuration Resolver Plugin
====================================

This plugin adds a `resolveConfigurations` task that resolves all dependencies in a project. This is
useful for resolving all dependencies to cache in CI environments or for ensuring that all
dependencies have been resolved into the local Gradle cache for offline development.

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
        classpath 'gradle.plugin.com.palantir.configurationresolver:gradle-configuration-resolver-plugin:<version>'
    }
}

apply plugin: 'com.palantir.configuration-resolver'
```

Or use the plugins closure:

```Gradle
plugins {
    id 'com.palantir.configuration-resolver' version '<version>'
}
```

This will add the `resolveConfigurations` task to the project.

License
-------
This project is made available under the [Apache 2.0 License][license].


[license]: http://www.apache.org/licenses/LICENSE-2.0
