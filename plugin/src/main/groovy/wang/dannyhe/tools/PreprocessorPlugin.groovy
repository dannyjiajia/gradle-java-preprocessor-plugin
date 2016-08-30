package wang.dannyhe.tools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException

public class PreprocessorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new ProjectConfigurationException("PreprocessorPlugin should be applyed after the android plugin.",new java.lang.Throwable("void apply(Project project)"))
        }
        //init the extensions
        project.extensions.create('preprocessor', PluginGlobalSettingExtension)
        project.android.productFlavors.whenObjectAdded { flavor ->
            flavor.extensions.create("processor", FlavorExtension, "")
        }

        project.afterEvaluate {
            configProject(project)
        }
    }

    void configProject(Project project) {
        project.android.applicationVariants.all { variant ->
            variant.productFlavors.each { flavor ->
                def badConfig = (flavor.processor.sourceDir == null && project.preprocessor.sourceDir == null) || (project.preprocessor.targetDir == null && flavor.processor.targetDir == null)
                if (!badConfig)
                {
                    def finalSourceDir = flavor.processor.sourceDir == null ? project.preprocessor.sourceDir : flavor.processor.sourceDir
                    def finalTargetDir = flavor.processor.targetDir == null ? project.preprocessor.targetDir : flavor.processor.targetDir
                    def finalVerbose = project.preprocessor ? project.preprocessor.verbose : true
                    def groupName = project.preprocessor ? project.preprocessor.groupName : "preprocessor"
                    final def processorTaskName = "preprocess${flavor.name.capitalize()}${variant.buildType.name.capitalize()}"
                    project.task(processorTaskName,type:PreprocessorTask) {
                        symbols flavor.processor.symbols
                        verbose finalVerbose
                        sourceDir finalSourceDir
                        targetDir finalTargetDir
                        group groupName
                        description "Preprocess java source code for ${flavor.name.capitalize()} ${variant.buildType.name.capitalize()}."
                    }
                    variant.javaCompile.dependsOn processorTaskName
                }else {
                    throw new ProjectConfigurationException("plugin can't create the task on ${flavor.name.capitalize()} flavor,because of bad config.",new java.lang.Throwable("void configProject(Project project)"))
                }
            }
        }
    }
}

class FlavorExtension {
    String symbols
    File sourceDir
    File targetDir

    FlavorExtension(String symbols){
        this.symbols = symbols
    }

    String getSymbols() {
        return symbols
    }

    void setSymbols(String symbols) {
        this.symbols = symbols
    }

    File getSourceDir() {
        return sourceDir
    }

    File getTargetDir() {
        return targetDir
    }

    void setSourceDir(File sourceDir) {
        this.sourceDir = sourceDir
    }

    void setTargetDir(File targetDir) {
        this.targetDir = targetDir
    }
}

class PluginGlobalSettingExtension {
    File sourceDir
    File targetDir
    boolean verbose = true
    String groupName = "preprocessor"
}