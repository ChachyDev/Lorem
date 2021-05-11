package club.chachy.auth.ms.data

data class MCStoreResponse(val items: List<Product>)

data class Product(
    val name: String,
    val signature: String
) // In a real world situation we don't actually care about these we just wanna check if they're existent

data class MCProfileResponse(val id: String, val name: String)