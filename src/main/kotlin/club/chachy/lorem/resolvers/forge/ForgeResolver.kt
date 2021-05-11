package club.chachy.lorem.resolvers.forge

import club.chachy.lorem.launch.manifest.Library
import club.chachy.lorem.resolvers.Resolver
import club.chachy.lorem.utils.getOrNull
import com.google.gson.JsonObject
import java.io.File

object ForgeResolver : Resolver {
    override fun resolveLibrary(library: JsonObject): Library {
        val name = library["name"].asString

        if (name.startsWith("net.minecraftforge:forge")) {
            val url = library["url"].asString

            val path = name.split(":")

            val builtPath =
                path[0].replace(".", File.separator) + File.separatorChar + path[1] + File.separatorChar + path[2]
            val urlPath = url + builtPath.replace(File.separator, "/") // Windows moment
            val jarName = path[1] + "-" + path[2] + "-universal.jar"

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


        val url = library.getOrNull("url")?.asString ?: "https://libraries.minecraft.net/"

        val path = name.split(":")

        val builtPath =
            path[0].replace(".", File.separator) + File.separatorChar + path[1] + File.separatorChar + path[2]
        val urlPath = url + builtPath.replace(File.separator, "/") // Windows moment
        val jarName = path[1] + "-" + path[2] + ".jar"

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