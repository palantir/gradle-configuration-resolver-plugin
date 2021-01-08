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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolveDetails

class ConfigurationResolverPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create("configurationResolver", ConfigurationResolverPluginExtension)

        project.configurations.all {
            resolutionStrategy {
                eachDependency { DependencyResolveDetails details ->
                    project.configurationResolver.allDeps.add([group: details.target.group, name: details.target.name, version: details.target.version])
                }
            }
        }

        project.tasks.register("resolveConfigurations", ResolveConfigurationsTask.class)
    }

}
