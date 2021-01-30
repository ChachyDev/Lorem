package club.chachy.lorem.utils

import com.google.gson.JsonArray

fun JsonArray.toBoolean(): Boolean {
    val size = size()
    var booleanArray = BooleanArray(size)
    map { it.asJsonObject }.forEach {
        if (size == 1) {
            when (it["action"].asString) {
                "allow" -> {
                    if (it.has("os")) {
                        val os = it["os"].asJsonObject
                        if (os.has("name")) {
                            var systemOs = System.getProperty("os.name")
                            if (systemOs.equals("Mac OS X", true)) systemOs = "osx"
                            return systemOs == os["name"].asString
                        }
                        if (os.has("arch")) {
                            val arch = os["arch"].asString
                            val a = System.getProperty("os.arch")
                            val convert = if (a == "amd64") "x64" else "x86"
                            return convert == arch
                        }
                    }
                }

                "disallow" -> {
                    if (it.has("os")) {
                        val os = it["os"].asJsonObject
                        if (os.has("name")) {
                            var systemOs = System.getProperty("os.name")
                            if (systemOs.equals("Mac OS X", true)) systemOs = "osx"
                            return systemOs != os["name"].asString
                        }
                        if (os.has("arch")) {
                            val arch = os["arch"].asString
                            val a = System.getProperty("os.arch")
                            val convert = if (a == "amd64") "x64" else "x86"
                            return convert != arch
                        }
                    }
                }
            }
        } else {
            when (it["action"].asString) {
                "allow" -> {
                    if (it.has("os")) {
                        val os = it["os"].asJsonObject["name"].asString
                        if (it.has("version")) {
                            val version = it["os"].asJsonObject["version"].asString.toString()
                            val str = System.getProperty("os.version")
                            if (str.substring(0, str.length - 1).matches(version.toRegex())) {
                                var systemOs = System.getProperty("os.name")
                                if (systemOs.equals("Mac OS X", true)) systemOs = "osx"
                                if (systemOs.contains("Windows", true)) systemOs = "windows"
                                booleanArray += systemOs == os
                            }
                        }
                    }
                }

                "disallow" -> {
                    if (it.has("os")) {
                        val os = it["os"].asJsonObject["name"].asString
                        var systemOs = System.getProperty("os.name")
                        if (systemOs.equals("Mac OS X", true)) systemOs = "osx"
                        booleanArray += systemOs != os
                    }
                }
            }
        }
    }

    // Default
    return booleanArray.majority
}

val BooleanArray.majority: Boolean
    get() {
        var trues = 0
        var falsies = 0
        forEach { if (it) trues += 1 else falsies += 1 }
        return trues > falsies
    }