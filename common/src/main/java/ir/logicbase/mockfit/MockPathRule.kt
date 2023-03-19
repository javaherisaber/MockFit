package ir.logicbase.mockfit

public data class MockPathRule(
    val method: String,
    val route: String,
    val responseFile: String,
    val includeQueries: Array<String> = arrayOf(),
    val excludeQueries: Array<String> = arrayOf(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MockPathRule

        if (method != other.method) return false
        if (route != other.route) return false
        if (responseFile != other.responseFile) return false
        if (!includeQueries.contentEquals(other.includeQueries)) return false
        if (!excludeQueries.contentEquals(other.excludeQueries)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = method.hashCode()
        result = 31 * result + route.hashCode()
        result = 31 * result + responseFile.hashCode()
        result = 31 * result + includeQueries.contentHashCode()
        result = 31 * result + excludeQueries.contentHashCode()
        return result
    }
}
