package libs

interface Failure{
    val errorMessage: String
}



// All below objects should be updated to be their NavSdk look a likes.
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

data class UniqueId(
    val id: Long,
)

data class RouteId(
    val uniqueId: UniqueId,
)

data class RouteData(
    val amountOfTraffic: List<Unit>,
)