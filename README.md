Gradle Configuration Resolver Plugin
====================================

 [ ![Download](https://api.bintray.com/packages/palantir/releases/gradle-configuration-resolver-plugin/images/download.svg) ](https://bintray.com/palantir/releases/gradle-configuration-resolver-plugin/_latestVersion)


This plugin adds a `resolveConfigurations` task that resolves all dependencies in a project. This is
useful for resolving all dependencies to cache in CI environments or for ensuring that all
dependencies have been resolved into the local Gradle cache for offline development.

The dependency information is also added to a list named `allDeps` in the `configurationResolver`
extension container of the project. The `allDeps` list can be used to programmatically process
dependencies elsewhere in the buildscript. The `allDeps` list is populated when the dependencies are
resolved.

Usage
-----

Apply the plugin using either the buildscript closure and an `apply plugin`:

```Gradle
buildscript {
    repositories {
        maven { url 'https://palantir.bintray.com/releases/' }
    }

    dependencies {
        classpath 'com.palantir.configurationresolver:gradle-configuration-resolver-plugin:<version>'
    }
}

// apply this plugin to any projects that should be included. for many, this will be all projects
allprojects {
	apply plugin: 'com.palantir.configuration-resolver'
}
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
