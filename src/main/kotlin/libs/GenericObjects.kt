package libs

import java.time.ZonedDateTime
import kotlin.time.Duration

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

data class RouteStatus(
    val routeId: RouteId,
    val routeName: String,
    val routeStatus: String,
    val waypoints: List<GeoPoint>,
    val travelTimeSeconds: Int,
    val createdAt: ZonedDateTime,
    val delayTimeSeconds: Int,
    val passable: Boolean,
    val routeLengthMeters: Int,
    val completeness: Int,
    val typicalTravelTimeCoverage: Int,
)