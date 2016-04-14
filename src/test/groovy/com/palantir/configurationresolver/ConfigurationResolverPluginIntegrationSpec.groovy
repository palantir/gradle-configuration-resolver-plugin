package com.palantir.configurationresolver

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import org.gradle.api.logging.LogLevel

class ConfigurationResolverPluginIntegrationSpec extends IntegrationSpec {

    def 'resolveConfigurations resolves compile and testCompile dependencies'() {
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

    @Override
    protected LogLevel getLogLevel() {
        return LogLevel.DEBUG
    }

}
