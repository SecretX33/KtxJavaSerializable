import com.github.secretx33.KtxJavaSerializableExtension
import com.github.secretx33.project
import com.google.auto.service.AutoService
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@AutoService(KotlinCompilerPluginSupportPlugin::class)
class KtxJavaSerializableGradleSubplugin : KotlinCompilerPluginSupportPlugin {

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        check(kotlinCompilation.platformType == KotlinPlatformType.jvm) { "'$PLUGIN_ID' can only be used in JVM targeted projects" }
        return true
    }

    /**
     * Namespace of the plugin arguments.
     */
    override fun getCompilerPluginId(): String = "ktxserializablejava"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.github.secretx33",
        artifactId = PLUGIN_ID,
        version = "0.1-SNAPSHOT",
    )

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.project
        val extension = project.extensions.getByType(KtxJavaSerializableExtension::class.java)
        return project.provider {
            listOf(SubpluginOption(key = "enabled", value = extension.enabled.toString()))
        }
    }

    companion object {
        const val PLUGIN_SHORT_ID = "ktx-serializable-java"
        const val PLUGIN_ID = "com.github.secretx33.$PLUGIN_SHORT_ID"
    }
}
