package club.chachy.lorem.resolvers.fabric

import club.chachy.lorem.launch.manifest.Library
import club.chachy.lorem.resolvers.Resolver
import com.google.gson.JsonObject
import java.io.File

/**
 * Due to using a custom format in the %version%.json we have to use a custom resolver to resolve their libraries.
 *
 * This resolver is t he main fabric resolver but also used as a fallback. In other words, if you want your custom client
 * to work without a custom resolver, you should either follow the Fabric specification on all Fabric supported versions
 * or the Mojang spec and on non Fabric versions, the Forge spec. See more about it at [club.chachy.lorem.resolvers.forge.ForgeResolver]
 * or the default spec. (Which is preferred).
 *
 * @author ChachyDev
 * @since 0.1-DEV
 */

object FabricResolver : Resolver {

    /**
     * Resolves the library from a JsonObject.
     * Once resolved the library data gets added in a [club.chachy.lorem.launch.manifest.VersionJsonProvider]
     * implementation to the classpath.
     *
     * @see club.chachy.lorem.launch.manifest.DefaultVersionJsonProvider for more info about these resolvers are used.
     */

    override fun resolveLibrary(library: JsonObject): Library {
        val name = library["name"].asString
        val url = library["url"].asString

        val path = name.split(":")

        val builtPath =
            path[0].replace(".", File.separator) + File.separatorChar + path[1] + File.separatorChar + path[2]
        val urlPath = url + builtPath.replace(File.separator, "/") // Windows moment
        val jarName = path[1] + "-" + path[2] + ".jar"

        // Return invalid size & sha1 because they aren't used.

        return Library(
            builtPath + File.separatorChar + jarName,
            urlPath + "/${jarName}",
            0,
            null,
            true,
            isNative = false
        )
    }
}
