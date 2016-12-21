package saluki-gradle-plugin

import org.gradle.api.Project
import org.gradle.api.Plugin

/**
 * @author shimingliu
 * Created: Wed Dec 21 18:13:53 CST 2016
 */
class Saluki-gradle-pluginPlugin implements Plugin<Project> {
   void apply (Project project) {
      project.convention.plugins.Saluki-gradle-pluginPlugin = new Saluki-gradle-pluginPluginConvention()
      
      // add your plugin tasks here.
   }
}