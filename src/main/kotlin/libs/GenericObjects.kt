package libs

import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

interface Failure{
    val errorMessage: String
}

@Serializable
data class CreateRouteParameters(
    val start: GeoPoint,
    val destination: GeoPoint,
    val channelId: String,
)

// All below objects should be updated to be their NavSdk look a likes.
@Serializable
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
    val routeId: Long,
    val routeName: String,
    val routeStatus: String,
    val routePathPoints: List<GeoPoint>,
    val travelTimeSeconds: Int,
    val createdAt: ZonedDateTime,
    val typicalTravelTimeSeconds: Int,
    val delayTimeSeconds: Int,
    val passable: Boolean,
    val routeLength: Int,
    val completeness: Int,
    val typicalTravelTimeCoverage: Int,
)

@Serializable
data class IntermediateRouteStatus(
    val routeId: Long,
    val routeName: String,
    val routeStatus: String,
    val routePathPoints: List<GeoPoint>,
    val travelTime: Int,
    val createdAt: String,
    val typicalTravelTime: Int,
    val delayTime: Int,
    val passable: Boolean,
    val routeLength: Int,
    val completeness: Int,
    val typicalTravelTimeCoverage: Int,
)