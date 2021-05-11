package club.chachy.lorem.launch.manifest

import club.chachy.lorem.resolvers.fabric.FabricResolver
import club.chachy.lorem.resolvers.forge.ForgeResolver
import club.chachy.lorem.utils.getOrNull
import club.chachy.lorem.utils.toBoolean
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.io.File

/**
 * Open for interpretation.
 */

private val resolvers = mutableMapOf("fabric" to FabricResolver, "forge" to ForgeResolver)

class DefaultVersionJsonProvider(
    override val minimumLauncherVersion: Int,
    private val obj: JsonObject,
    private val runDir: File,
    private val jvmArgs: MutableList<String>
) : VersionJsonProvider {
    override val assetsIndex: AssetsIndex
        get() = obj["assetIndex"].asJsonObject.let {
            AssetsIndex(
                it["id"].asString,
                it["sha1"].asString,
                it["size"].asLong,
                it["totalSize"].asLong,
                it["url"].asString
            )
        }

    override val assets: String get() = obj["assets"].asString

    override val client: ClientProperty
        get() {
            val downloads = obj["downloads"].asJsonObject
            val client = downloads["client"].asJsonObject
            return ClientProperty(client["sha1"].asString, client["size"].asLong, client["url"].asString)
        }

    override val id: String get() = obj["id"].asString

    override val libraries
        get() : List<Library> {
            val libs = mutableListOf<Library>()
            val is64Bit = System.getProperty("os.arch") == "amd64"
            val arch = if (is64Bit) "64" else "32"
            if (minimumLauncherVersion <= 18) {
                val libraries = obj["libraries"].asJsonArray
                libraries.forEach {
                    val obj = it.asJsonObject
                    if (!obj.has("downloads")) {
                        // Probs fabric gotta double check tho
                        val lib =
                            resolvers[id.substringAfter('-')]?.resolveLibrary(obj) ?: ForgeResolver.resolveLibrary(obj)
                        if (lib.path != "no_resolver") {
                            libs.add(lib)
                        }
                    } else {
                        val download = it.asJsonObject["downloads"].asJsonObject
                        val artifact = if (download.has("artifact")) download["artifact"].asJsonObject else null
                        val rule = if (download.has("rules")) download["rules"].asJsonArray.toBoolean() else true
                        if (download.has("classifiers")) {
                            val classifiers = download["classifiers"].asJsonObject
                            val native = with(System.getProperty("os.name")) {
                                when {
                                    startsWith(
                                        "Windows",
                                        true
                                    ) -> if (!classifiers.has("natives-windows")) classifiers["natives-windows-$arch"] else classifiers["natives-windows"]
                                    startsWith("Mac", true) || startsWith("Darwin", true) -> classifiers["natives-osx"]
                                    else -> classifiers["natives-linux"]
                                }.asJsonObject
                            }

                            libs.add(
                                Library(
                                    native["path"].asString,
                                    native["sha1"].asString,
                                    native["size"].asLong,
                                    native["url"].asString,
                                    isAllowed = true,
                                    isNative = true
                                )
                            )
                        }

                        if (artifact != null) {
                            libs.add(
                                Library(
                                    artifact["path"].asString,
                                    artifact["sha1"].asString,
                                    artifact["size"].asLong,
                                    artifact["url"].asString,
                                    rule,
                                    false
                                )
                            )
                        }
                    }
                }
            } else {
                val libraries = obj["libraries"].asJsonArray
                libraries.forEach {
                    val o = it.asJsonObject
                    if (o.has("name") && !o.has("downloads")) {
                        // Probs fabric gotta double check tho
                        libs.add(
                            resolvers[id.substringAfter('-')]?.resolveLibrary(obj) ?: FabricResolver.resolveLibrary(obj)
                        )
                    } else {
                        val download = it.asJsonObject["downloads"].asJsonObject
                        val artifact = download["artifact"].asJsonObject
                        val rule = if (download.has("rules")) download["rules"].asJsonArray.toBoolean() else true
                        if (download.has("classifiers")) {
                            val classifiers = download["classifiers"].asJsonObject
                            val native = runCatching {
                                with(System.getProperty("os.name")) {
                                    when {
                                        startsWith(
                                            "Windows",
                                            true
                                        ) -> if (!classifiers.has("natives-windows")) classifiers["natives-windows-$arch"] else classifiers["natives-windows"]
                                        startsWith("Mac", true) || startsWith(
                                            "Darwin",
                                            true
                                        ) -> classifiers["natives-osx"]
                                        else -> classifiers["natives-linux"]
                                    }.asJsonObject
                                }
                            }.getOrNull()

                            if (native != null) {
                                libs.add(
                                    Library(
                                        native["path"].asString,
                                        native["sha1"].asString,
                                        native["size"].asLong,
                                        native["url"].asString,
                                        isAllowed = true,
                                        isNative = true
                                    )
                                )
                            }
                        }
                        libs.add(
                            Library(
                                artifact["path"].asString,
                                artifact["sha1"].asString,
                                artifact["size"].asLong,
                                artifact["url"].asString,
                                rule,
                                false
                            )
                        )
                    }
                }
            }
            return libs
        }

    override val logging: Logging
        get() {
            val client = obj["logging"].asJsonObject["client"].asJsonObject
            val file = client["file"].asJsonObject
            return Logging(
                true,
                client["argument"].asString,
                file["id"].asString,
                file["sha1"].asString,
                file["size"].asLong,
                file["url"].asString,
                client["type"].asString
            )
        }

    override val mainClass: String get() = obj["mainClass"].asString

    override val arguments: Array<String>
        get() {
            val list = mutableListOf<String>()

            val mcArgs = mutableListOf<String>()

            val libs = mutableListOf<String>()

            val jar = obj.getOrNull("jar")?.asString ?: id

            libs.addAll(
                libraries
                    .mapNotNull { if (!it.isNative) File(File(runDir, "libraries"), it.path) else null }
                    .toMutableList()
                    .apply { add(0, File(File(File(runDir, "versions"), jar), "$jar.jar")) }
                    .map { it.absolutePath }
            )


            if (minimumLauncherVersion <= 18) {
                // Do version specific stuff here. ;)
                val args = obj["minecraftArguments"].asString
                args.split("--").forEach {
                    val kv = it.split(" ")
                    if (kv.size != 1) {
                        mcArgs.add("--" + kv[0])
                        mcArgs.add(kv[1])
                    }
                }

                jvmArgs.add("-Djava.library.path={natives_directory}")
                jvmArgs.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump")
                jvmArgs.add("-cp")
                jvmArgs.add("\${classpath}")
            } else {
                val mainArguments = obj["arguments"].asJsonObject
                val gameArgument = mainArguments["game"].asJsonArray
                val jvmArguments = mainArguments["jvm"].asJsonArray
                gameArgument.forEach {
                    when (it) {
                        is JsonPrimitive -> {
                            // This is a string arg lets go baby!
                            mcArgs.add(it.asString)
                        }

                        is JsonObject -> {
                            val value = it.asJsonObject["value"].let { v ->
                                when (v) {
                                    is JsonArray -> v.asJsonArray.map { a -> a.asString }.toTypedArray()
                                    is JsonPrimitive -> arrayOf(v.asString)
                                    else -> error("Something went wrong...")
                                }
                            }

                            val rule = it.asJsonObject["rules"].asJsonArray
                            if (rule.toBoolean()) {
                                mcArgs.addAll(value)
                            }
                        }

                        else -> error("Something went wrong...")
                    }
                }

                jvmArguments.forEach {
                    when (it) {
                        is JsonPrimitive -> {
                            // Good!
                            jvmArgs.add(it.asString)
                        }

                        is JsonObject -> {
                            // No!
                            val rule = it.asJsonObject["rules"].asJsonArray.toBoolean()
                            if (rule) {
                                val value = it.asJsonObject["value"].let { v ->
                                    when (v) {
                                        is JsonArray -> v.asJsonArray.map { a -> a.asString }.toTypedArray()
                                        is JsonPrimitive -> arrayOf(v.asString)
                                        else -> error("Something went wrong...")
                                    }
                                }

                                jvmArgs.addAll(value)
                            }
                        }
                    }
                }
            }

            jvmArgs[jvmArgs.indexOf("\${classpath}")] = libs.joinToString(File.pathSeparator)

            // Build list
            list.addAll(jvmArgs)
            list.add(mainClass)
            list.addAll(mcArgs)

            return list.toTypedArray()
        }
    override val type: String = obj["type"].asString
}