
import com.github.secretx33.KtxJavaSerializableConfiguration
import com.github.secretx33.KtxJavaSerializableConfiguration.Companion.DEFAULT_SERIAL_VERSION_UID_OPTION
import com.github.secretx33.KtxJavaSerializableConfiguration.Companion.ENABLED_OPTION
import com.github.secretx33.getByType
import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@Suppress("unused") // Used by service loader
@AutoService(KotlinCompilerPluginSupportPlugin::class)
class KtxJavaSerializableGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        check(kotlinCompilation.platformType == KotlinPlatformType.jvm) { "'$PLUGIN_ID' can only be used in JVM targeted projects" }
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType<KtxJavaSerializableConfiguration>()
        return extension.enabled
    }

    override fun apply(target: Project) {
        target.extensions.create("ktxJavaSerializable", KtxJavaSerializableConfiguration::class.java)
    }

    /**
     * Namespace of the plugin arguments.
     */
    override fun getCompilerPluginId(): String = PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.github.secretx33",
        artifactId = PLUGIN_ID,
        version = "0.1-SNAPSHOT",
    )

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.project
        val extension = project.extensions.getByType<KtxJavaSerializableConfiguration>()

        return project.provider {
            listOf(
                SubpluginOption(key = ENABLED_OPTION, value = extension.enabled.toString()),
                SubpluginOption(key = DEFAULT_SERIAL_VERSION_UID_OPTION, value = extension.defaultSerialVersionUID.toString()),
            )
        }
    }

    companion object {
        const val PLUGIN_SHORT_ID = "ktx-serializable-java"
        const val PLUGIN_ID = "io.github.secretx33.$PLUGIN_SHORT_ID"
    }
}
