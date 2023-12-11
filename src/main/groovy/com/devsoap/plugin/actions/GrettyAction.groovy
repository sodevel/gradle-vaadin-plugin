/*
 * Copyright 2018 John Ahlroos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.devsoap.plugin.actions

import com.devsoap.plugin.GradleVaadinPlugin
import com.devsoap.plugin.Util
import com.devsoap.plugin.tasks.CompileThemeTask
import com.devsoap.plugin.tasks.CompileWidgetsetTask
import com.devsoap.plugin.tasks.RunTask
import org.gradle.api.Incubating
import org.gradle.api.Project

/**
 * Support for the Gretty plugin
 */
@Incubating
class GrettyAction extends PluginAction{

    private static final String GRETTY_CONFIGURATION = 'gretty'
    private static final String GRETTY_RUN_TASK = 'appRun'
    private static final String GRETTY_EXTENSION = GRETTY_CONFIGURATION

    @Override
    String getPluginId() {
        'org.gretty'
    }

    @Override
    protected void executeAfterEvaluate(Project project) {
        super.executeAfterEvaluate(project)

        project.tasks.findByName(GRETTY_RUN_TASK).dependsOn(CompileWidgetsetTask.NAME, CompileThemeTask.NAME)

        // Delegate to bootRun if spring boot is present
        project.tasks.findByName(RunTask.NAME).dependsOn(GRETTY_RUN_TASK)

        project.configurations[GRETTY_CONFIGURATION].extendsFrom(
            project.configurations[GradleVaadinPlugin.CONFIGURATION_SERVER]
        )

        project.configurations[GRETTY_CONFIGURATION].extendsFrom(
            project.configurations[GradleVaadinPlugin.CONFIGURATION_CLIENT_COMPILE]
        )

        if (!Util.getWidgetset(project)) {
            project.configurations[GRETTY_CONFIGURATION].extendsFrom(
                project.configurations[GradleVaadinPlugin.CONFIGURATION_CLIENT]
            )
        }

        if (Util.isPushEnabled(project)) {
            project.configurations[GRETTY_CONFIGURATION].extendsFrom(
                project.configurations[GradleVaadinPlugin.CONFIGURATION_PUSH]
            )
        }
    }

    /**
     * Is Gretty present in the project
     *
     * @param project
     *      the project to check
     */
    static boolean isGrettyPresent(Project project) {
        project.extensions.findByName(GRETTY_EXTENSION)
    }
}
