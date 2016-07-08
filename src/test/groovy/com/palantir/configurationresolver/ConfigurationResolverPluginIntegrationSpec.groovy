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

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import org.gradle.api.logging.LogLevel

class ConfigurationResolverPluginIntegrationSpec extends IntegrationSpec {

    private LogLevel logLevel
    def setup() {
        logLevel = LogLevel.QUIET
    }

    def 'resolveConfigurations resolves compile and testCompile dependencies'() {
        logLevel = LogLevel.DEBUG
        buildFile << '''
            apply plugin: 'java'
            apply plugin: 'com.palantir.configuration-resolver'

            repositories {
                mavenCentral()
            }

            dependencies {
                compile 'com.google.guava:guava:19.0'
                testCompile 'junit:junit:4.12'
            }
        '''.stripIndent()

        when:
        ExecutionResult result = runTasks('resolveConfigurations')

        then:
        result.success
        result.standardOutput =~ 'Using com.google.guava:guava:19.0'
        result.standardOutput =~ 'Using junit:junit:4.12'
    }

    def 'resolveConfigurations resolves subproject dependencies'() {
        logLevel = LogLevel.DEBUG
        buildFile << '''
            subprojects {
                apply plugin: 'java'
                apply plugin: 'com.palantir.configuration-resolver'

                repositories {
                    mavenCentral()
                }
            }
        '''.stripIndent()

        addSubproject('subproject-1', '''
            dependencies {
                compile 'com.google.guava:guava:19.0'
            }
        '''.stripIndent())

        addSubproject('subproject-2', '''
            dependencies {
                testCompile 'junit:junit:4.12'
            }
        '''.stripIndent())

        when:
        ExecutionResult result = runTasks('resolveConfigurations')

        then:
        result.success
        result.standardOutput =~ 'Using com.google.guava:guava:19.0'
        result.standardOutput =~ 'Using junit:junit:4.12'
    }

    def 'applying plugin creates empty project.ext.allDeps list'() {
        buildFile << '''
            subprojects {
                apply plugin: 'java'
                apply plugin: 'com.palantir.configuration-resolver'

                repositories {
                    mavenCentral()
                }

                task printAllDeps << {
                    println "${project.name}: ${project.configurationResolver.allDeps}"
                }
            }
        '''.stripIndent()

        addSubproject('subproject-1', '''
            dependencies {
                compile 'com.google.guava:guava:19.0'
            }
        '''.stripIndent())

        addSubproject('subproject-2', '''
            dependencies {
                testCompile 'junit:junit:4.12'
            }
        '''.stripIndent())

        when:
        ExecutionResult result = runTasks('printAllDeps')

        then:
        result.success
        result.standardOutput =~ /subproject-1: \[\]/
        result.standardOutput =~ /subproject-2: \[\]/
    }

    def 'running resolveConfigurations populates project.ext.allDeps with dependencies'() {
        buildFile << '''
            subprojects {
                apply plugin: 'java'
                apply plugin: 'com.palantir.configuration-resolver'

                repositories {
                    mavenCentral()
                }

                task printAllDeps << {
                    println "${project.name}: ${project.configurationResolver.allDeps}"
                }
            }
        '''.stripIndent()

        addSubproject('subproject-1', '''
            dependencies {
                compile 'com.google.guava:guava:19.0'
            }
        '''.stripIndent())

        addSubproject('subproject-2', '''
            dependencies {
                testCompile 'junit:junit:4.12'
            }
        '''.stripIndent())

        when:
        ExecutionResult result = runTasks('resolveConfigurations', 'printAllDeps')

        then:
        result.success
        result.standardOutput =~ /\[\[group:com.google.guava, name:guava, version:19.0\]/
        result.standardOutput =~ /\[\[group:junit, name:junit, version:4.12\]/
    }

    @Override
    protected LogLevel getLogLevel() {
        return logLevel
    }

}
