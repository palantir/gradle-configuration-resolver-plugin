// Copyright 2015 Palantir Technologies
//
// Licensed under the Apache License, Version 2.0 (the "License")
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.palantir.configurationresolver

import nebula.test.IntegrationTestKitSpec
import nebula.test.functional.ExecutionResult
import org.gradle.api.logging.LogLevel
import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll

@Unroll
class ConfigurationResolverPluginIntegrationSpec extends IntegrationTestKitSpec {

    private static final List<String> GRADLE_VERSIONS = ["5.4.1", "6.0.1", "6.7.1"]

    private LogLevel logLevel
    def setup() {
//        debug = true
        logLevel = LogLevel.QUIET
    }

    def '#gradleVersionNumber: resolveConfigurations resolves compile and testCompile dependencies'() {
        gradleVersion = gradleVersionNumber
        logLevel = LogLevel.DEBUG
        buildFile << '''
            plugins {
                id 'java'
                id 'com.palantir.configuration-resolver'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                compile 'com.google.guava:guava:19.0'
                testCompile 'junit:junit:4.12'
            }
        '''.stripIndent()

        when:
        BuildResult result = runTasks('resolveConfigurations')

        then:
        result.output =~ 'Using com.google.guava:guava:19.0'
        result.output =~ 'Using junit:junit:4.12'

        where:
        gradleVersionNumber << GRADLE_VERSIONS
    }

    def '#gradleVersionNumber: resolveConfigurations resolves subproject dependencies'() {
        gradleVersion = gradleVersionNumber
        logLevel = LogLevel.DEBUG
        buildFile << '''
            subprojects {
                repositories {
                    mavenCentral()
                }
            }
        '''.stripIndent()

        addSubproject('subproject-1', '''
            plugins {
                id 'java'
                id 'com.palantir.configuration-resolver'
            }

            dependencies {
                implementation 'com.google.guava:guava:19.0'
            }
        '''.stripIndent())

        addSubproject('subproject-2', '''
            plugins {
                id 'java'
                id 'com.palantir.configuration-resolver'
            }

            dependencies {
                testImplementation 'junit:junit:4.12'
            }
        '''.stripIndent())

        when:
        BuildResult result = runTasks('resolveConfigurations')

        then:
        result.output =~ 'Using com.google.guava:guava:19.0'
        result.output =~ 'Using junit:junit:4.12'

        where:
        gradleVersionNumber << GRADLE_VERSIONS
    }

    def '#gradleVersionNumber: applying plugin creates empty project.ext.allDeps list'() {
        gradleVersion = gradleVersionNumber
        buildFile << '''
            subprojects {
                repositories {
                    mavenCentral()
                }

                task printAllDeps {
                    doLast {
                        println "${project.name}: ${project.configurationResolver.allDeps}"
                    }
                }
            }
        '''.stripIndent()

        addSubproject('subproject-1', '''
            plugins {
                id 'java'
                id 'com.palantir.configuration-resolver'
            }

            dependencies {
                implementation 'com.google.guava:guava:19.0'
            }
        '''.stripIndent())

        addSubproject('subproject-2', '''
            plugins {
                id 'java'
                id 'com.palantir.configuration-resolver'
            }

            dependencies {
                testImplementation 'junit:junit:4.12'
            }
        '''.stripIndent())

        when:
        BuildResult result = runTasks('printAllDeps')

        then:
        result.output =~ /subproject-1: \[\]/
        result.output =~ /subproject-2: \[\]/

        where:
        gradleVersionNumber << GRADLE_VERSIONS
    }

    def '#gradleVersionNumber: running resolveConfigurations populates project.ext.allDeps with dependencies'() {
        logLevel = LogLevel.WARN
        gradleVersion = gradleVersionNumber
        buildFile << '''
            subprojects {
                repositories {
                    mavenCentral()
                }

                task printAllDeps {
                    doLast {
                        println "${project.name}: ${project.configurationResolver.allDeps}"
                    }
                }
            }
        '''.stripIndent()

        addSubproject('subproject-1', '''
            plugins {
                id 'java'
                id 'com.palantir.configuration-resolver'
            }

            dependencies {
                implementation 'com.google.guava:guava:19.0'
            }
        '''.stripIndent())

        addSubproject('subproject-2', '''
            plugins {
                id 'java'
                id 'com.palantir.configuration-resolver'
            }

            dependencies {
                testImplementation 'junit:junit:4.12'
            }
        '''.stripIndent())

        when:
        BuildResult result = runTasks('resolveConfigurations', 'printAllDeps')
        println("result: " + result.output)

        then:
        result.output =~ /\[\[group:com.google.guava, name:guava, version:19.0\]/
        result.output =~ /\[\[group:junit, name:junit, version:4.12\]/

        where:
        gradleVersionNumber << GRADLE_VERSIONS
    }

    @Override
    LogLevel getLogLevel() {
        return logLevel
    }

}
