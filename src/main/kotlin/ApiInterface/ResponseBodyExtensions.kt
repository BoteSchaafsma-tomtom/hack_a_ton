package ApiInterface

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import libs.IntermediateRouteStatus
import libs.RouteStatus
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.minutes

fun RouteStatus.hasAnomalies(): Boolean {
    // Can't drive route
    if (!passable) {
        return true
    }

    // Route too short to tell anything.
    if (typicalTravelTimeSeconds < 6.minutes.inWholeSeconds) {
        return false
    }

    // More than 15% delay time.
    return (((travelTimeSeconds - delayTimeSeconds) * 100) / delayTimeSeconds) > 15
}

private fun IntermediateRouteStatus.toRouteStatus() = RouteStatus(
    routeId = routeId,
    routeName = routeName,
    routeStatus = routeStatus,
    routePathPoints = routePathPoints,
    travelTimeSeconds = travelTime,
    createdAt = ZonedDateTime.parse(createdAt),
    typicalTravelTimeSeconds = typicalTravelTime,
    delayTimeSeconds = delayTime,
    passable = passable,
    routeLength = routeLength,
    completeness = completeness,
    typicalTravelTimeCoverage = typicalTravelTimeCoverage,
)

fun String.toRouteStatus(): List<RouteStatus>? {
    try {
        val intermediate = Json.decodeFromString<List<IntermediateRouteStatus>>(this)
        return intermediate.map { it.toRouteStatus() }
    } catch (e: SerializationException) {
        println("Error while parsing: $e")
    } catch (e: IllegalArgumentException) {
        println("Error while parsing: $e")
    }
    return null
}
