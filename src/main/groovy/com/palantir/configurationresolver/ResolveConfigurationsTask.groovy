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

import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.TaskAction

public class ResolveConfigurationsTask extends AbstractTask {

    @TaskAction
    public void resolveAllConfigurations() {
        project.configurations.all { configuration ->
            // New versions of Gradle have unresolvable configurations by default
            // https://github.com/gradle/gradle/pull/1351
            if (configuration.metaClass.respondsTo(configuration, "isCanBeResolved") &&
                !configuration.isCanBeResolved()) {
              return;
            }
            configuration.resolve()
        }
    }

}
