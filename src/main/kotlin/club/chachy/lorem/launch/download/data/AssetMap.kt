package club.chachy.lorem.launch.download.data

import java.util.*

class AssetMap : HashMap<String, Asset>()

data class Asset(val hash: String, val size: Long)