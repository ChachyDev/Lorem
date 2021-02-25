package club.chachy.lorem.launch.manifest

import club.chachy.lorem.launch.Task
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

class CustomManifestTask(private val runDir: File, private val jvmArgs: MutableList<String>) :
    Task<String, VersionJsonProvider> {
    override suspend fun execute(data: String): VersionJsonProvider {
        val jsonFile = File(runDir, "versions/$data/$data.json")
        if (!jsonFile.exists()) error("Failed to detect custom manifest's file")

        val parsed = JsonParser.parseString(jsonFile.readText()).asJsonObject

        val inheritedVersion = parsed["inheritsFrom"].asString

        val inheritedJson = runCatching {
            JsonParser.parseString(
                File(
                    runDir,
                    "versions/$inheritedVersion/$inheritedVersion.json"
                ).readText()
            ).asJsonObject
        }.getOrNull()
            ?: error("Please run the inherited version (${inheritedVersion}) before trying to run a custom client")

        val merged = parsed.merge(inheritedJson)

        return DefaultVersionJsonProvider(merged["minimumLauncherVersion"].asInt, merged, runDir, jvmArgs)
    }
}

fun JsonObject.merge(obj: JsonObject): JsonObject {
    val priorityJson = this

    val set = priorityJson.entrySet()

    val newJson = JsonObject()

    set.forEach {
        if (it.value.isJsonArray) {
            if (it.value.asJsonArray.size() != 0) {
                newJson.add(it.key, it.value)
            }
        } else {
            newJson.add(it.key, it.value)
        }
    }

    obj.entrySet().forEach {
        if (!newJson.has(it.key) || newJson.has(it.key) && it.value.isJsonObject) {
            newJson.add(it.key, it.value)
        }

        if (newJson.has(it.key) && it.value.isJsonArray) {
            val array = newJson.getAsJsonArray(it.key)
            it.value.asJsonArray.forEach { element ->
                array.add(element)
            }
            newJson.remove(it.key)
            newJson.add(it.key, array)
        }
    }

    return newJson
}
