package wang.dannyhe.tools

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
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
            flavor.extensions.create("processor", FlavorExtension)
        }

        project.afterEvaluate {
            configProject(project)
        }
    }

    void configProject(Project project) {
        PluginGlobalSettingExtension global = project.preprocessor
        if(global.sourceDir == null || global.targetDir == null) {
            throw new ProjectConfigurationException("must set default sourceDir and targetDir in global preprocessor.",new java.lang.Throwable("void configProject(Project project)"))
        }
        project.android.applicationVariants.all { variant ->
            final def processorTaskName = "${variant.name.capitalize()}-preprocess"
            def finalFlavorExtensions = new ArrayList<FlavorExtension>()
            if(variant.productFlavors.size() > 0) {
                variant.productFlavors.each { flavor ->
                    finalFlavorExtensions.add(flavor.processor)
                }
            }

            def fSymbols = new HashSet<String>()
            finalFlavorExtensions.each {flavorExt->
                fSymbols.addAll(flavorExt.symbols)
            }
            fSymbols.addAll(global.symbols)
            project.task(processorTaskName,type:PreprocessorTask) {
                symbols fSymbols.join(",")
                verbose global.verbose
                sourceDir global.sourceDir
                targetDir  global.targetDir
                group global.groupName
                description "Preprocess java source code for ${processorTaskName}:${fSymbols.join(",")}"
            }
            variant.javaCompile.dependsOn processorTaskName

        }
    }
}

@EqualsAndHashCode
@ToString
class FlavorExtension {
    String[] symbols = [""] as String[]
//    File sourceDir
//    File targetDir
//    String name
}

class PluginGlobalSettingExtension {
    File sourceDir
    File targetDir
    boolean verbose = true
    String groupName = "preprocessor"
    String[] symbols = [""] as String[]


}