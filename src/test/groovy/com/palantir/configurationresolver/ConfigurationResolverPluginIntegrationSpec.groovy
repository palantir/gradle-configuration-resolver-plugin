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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ConfigurationResolverPluginIntegrationSpec extends Specification {

    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    File settingsFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        settingsFile = testProjectDir.newFile('settings.gradle')
    }

    def 'resolveConfigurations resolves compile and testCompile dependencies'() {
        buildFile << '''
            plugins {
                id 'com.palantir.configuration-resolver'
            }

            apply plugin: 'java'

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
        result.task(':resolveConfigurations').outcome == TaskOutcome.SUCCESS
        result.output =~ 'Using com.google.guava:guava:19.0'
        result.output =~ 'Using junit:junit:4.12'
    }

    def 'resolveConfigurations resolves subproject dependencies'() {
        buildFile << '''
            plugins {
                id 'com.palantir.configuration-resolver'
            }

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
        BuildResult result = runTasks('resolveConfigurations')

        then:
        result.task(':resolveConfigurations').outcome == TaskOutcome.SUCCESS
        result.task(':subproject-1:resolveConfigurations').outcome == TaskOutcome.SUCCESS
        result.task(':subproject-2:resolveConfigurations').outcome == TaskOutcome.SUCCESS
        result.output =~ 'Using com.google.guava:guava:19.0'
        result.output =~ 'Using junit:junit:4.12'
    }

    def 'applying plugin creates empty project.ext.allDeps list'() {
        buildFile << '''
            plugins {
                id 'com.palantir.configuration-resolver'
            }

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
        BuildResult result = runTasks('printAllDeps')

        then:
        result.task(':subproject-1:printAllDeps').outcome == TaskOutcome.SUCCESS
        result.task(':subproject-2:printAllDeps').outcome == TaskOutcome.SUCCESS
        result.output =~ /\[system.out\] subproject-1: \[\]/
        result.output =~ /\[system.out\] subproject-2: \[\]/
    }

    def 'running resolveConfigurations populates project.ext.allDeps with dependencies'() {
        buildFile << '''
            plugins {
                id 'com.palantir.configuration-resolver'
            }

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
        BuildResult result = runTasks('resolveConfigurations', 'printAllDeps')

        then:
        println result.tasks
        result.output =~ /\[\[group:com.google.guava, name:guava, version:19.0\]/
        result.output =~ /\[\[group:junit, name:junit, version:4.12\]/
    }

    private BuildResult runTasks(String... tasks) {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(['--debug'] + tasks.toList())
                .withPluginClasspath()
                .withDebug(true)
                .build()
    }

    private void addSubproject(String subprojectName, String subprojectBuidFileContents) {
        File subprojectFolder = testProjectDir.newFolder(subprojectName)
        subprojectFolder.toPath().resolve('build.gradle').toFile().text = subprojectBuidFileContents
        settingsFile << "include '${subprojectName}'${System.lineSeparator()}"
    }

}
