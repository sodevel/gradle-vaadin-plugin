package com.devsoap.plugin.tests

import com.devsoap.plugin.tasks.UpdateWidgetsetTask
import org.junit.Test

import java.nio.file.Paths

import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

/**
 * Created by john on 20.1.2016.
 */
class UpdateWidgetsetTest extends IntegrationTest {

    @Test void 'No Widgetset generated without property'() {
        runWithArguments(UpdateWidgetsetTask.NAME)
        assertFalse widgetsetFile.exists()
    }

    @Test void 'No Widgetset generated when widgetset management off'() {
        buildFile << "vaadinCompile.widgetset = 'com.example.MyWidgetset'\n"
        buildFile << "vaadinCompile.manageWidgetset = false"
        runWithArguments(UpdateWidgetsetTask.NAME)
        assertFalse widgetsetFile.exists()
    }

    @Test void 'Widgetset generated into resource folder'() {
        buildFile << "vaadinCompile.widgetset = 'com.example.MyWidgetset'"
        runWithArguments(UpdateWidgetsetTask.NAME)
        assertTrue widgetsetFile.exists()
    }

    @Test void 'Widgetset file contains addon widgetset inherits'() {
        buildFile << "vaadinCompile.widgetset = 'com.example.MyWidgetset'\n"
        buildFile << """
            dependencies {
                implementation 'org.vaadin.addons:qrcode:+'
            }
        """

        runWithArguments(UpdateWidgetsetTask.NAME)
        assertTrue widgetsetFile.text.contains('<inherits name="fi.jasoft.qrcode.QrcodeWidgetset" />')
    }

    @Test void 'Widgetset file contains inherits from sub-project dependencies'() {
        buildFile << "vaadinCompile.widgetset = 'com.example.MyWidgetset'\n"

        // Setup project 1
        File project1Dir = projectDir.newFolder('project1')
        project1Dir.mkdirs()
        File buildFile1 = makeBuildFile(project1Dir)
        buildFile1 << """
            dependencies {
                implementation 'org.vaadin.addons:qrcode:+'
            }
        """

        // Setup project 2
        File project2Dir = projectDir.newFolder('project2')
        project2Dir.mkdirs()
        File buildFile2 = makeBuildFile(project2Dir)
        buildFile2 << "vaadinCompile.widgetset = 'com.example.MyWidgetset'\n"
        buildFile2 << """
            dependencies {
                implementation project(':project1')
            }
        """

        // Setup settings.gradle
        settingsFile << """
            include 'project1'
            include 'project2'
        """

        runWithArguments(':project2:' + UpdateWidgetsetTask.NAME)
        assertTrue getWidgetsetFile(project2Dir).text.contains('<inherits name="fi.jasoft.qrcode.QrcodeWidgetset" />')
    }

    @Test void 'AppWidgetset created when project contains addon dependencies'() {
        buildFile << """
            dependencies {
                implementation 'org.vaadin.addons:qrcode:+'
            }
        """
        runWithArguments(UpdateWidgetsetTask.NAME)
        assertTrue appWidgetsetFile.exists()
        assertTrue appWidgetsetFile.text.contains('<inherits name="fi.jasoft.qrcode.QrcodeWidgetset" />')
    }

    @Test void 'AppWidgetset created when dependant project contains widgetset file'() {

        // Setup addon project
        File project1Dir = projectDir.newFolder('project1')
        project1Dir.mkdirs()

        File widgetsetFile = getWidgetsetFile(project1Dir)
        widgetsetFile.parentFile.mkdirs()
        widgetsetFile.createNewFile()
        widgetsetFile.text = """
            <module>
                <inherits name="com.vaadin.DefaultWidgetSet" />
            </module>
        """.stripIndent()

        File buildFile1 = makeBuildFile(project1Dir)
        buildFile1 << "vaadinCompile.widgetset = 'com.example.MyWidgetset'\n"

        // Setup demo project
        File project2Dir = projectDir.newFolder('project2')
        project2Dir.mkdirs()
        File buildFile2 = makeBuildFile(project2Dir)
        buildFile2 << """
            dependencies {
                implementation project(':project1')
            }
        """

        // Setup settings.gradle
        settingsFile << """
            include 'project1'
            include 'project2'
        """

        runWithArguments(':project2:' + UpdateWidgetsetTask.NAME)
        assertTrue getAppWidgetsetFile(project2Dir).text.contains('<inherits name="com.example.MyWidgetset" />')
    }

    @Test void 'If legacy mode, use compatibility widgetset'() {
        buildFile << "vaadinCompile.widgetset = 'com.example.MyWidgetset'\n"
        buildFile << """
            dependencies {
                implementation("com.vaadin:vaadin-compatibility-server:8.0.0")
                implementation("com.vaadin:vaadin-compatibility-client:8.0.0")
                implementation("com.vaadin:vaadin-compatibility-shared:8.0.0")
                implementation 'org.vaadin.addons:qrcode:+'
            }
        """

        runWithArguments(UpdateWidgetsetTask.NAME)
        assertTrue widgetsetFile.text.contains('<inherits name="com.vaadin.v7.Vaadin7WidgetSet" />')
        assertFalse widgetsetFile.text.contains('<inherits name="com.vaadin.DefaultWidgetSet" />')
    }

    private File getWidgetsetFile(File projectDir = this.projectDir.root, String fileName='MyWidgetset') {
        Paths.get(projectDir.canonicalPath,
                'src', 'main', 'resources', 'com', 'example', "${fileName}.gwt.xml").toFile()
    }

    private File getAppWidgetsetFile(File projectDir = this.projectDir.root) {
        Paths.get(projectDir.canonicalPath,'src', 'main', 'resources', "AppWidgetset.gwt.xml").toFile()
    }
}
