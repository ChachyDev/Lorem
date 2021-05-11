package club.chachy.lorem.resolvers

import club.chachy.lorem.launch.manifest.Library
import com.google.gson.JsonObject

interface Resolver {
    fun resolveLibrary(library: JsonObject): Library
}