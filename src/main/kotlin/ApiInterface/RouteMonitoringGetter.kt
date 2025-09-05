package ApiInterface

import kotlinx.io.IOException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import libs.GeoPoint
import libs.RouteId
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

class OkHttpRequest(
    private val httpClient: OkHttpClient,
) {
    fun GET(url: String, callback: Callback): Response {
        val request = Request.Builder().url(url).build()
        val call = httpClient.newCall(request)
        return call.execute()
    }

    fun post(url: String, jsonBody: String): Response {
        val mediaType = MediaType.parse("application/json")
        val requestBody: RequestBody = RequestBody.create(mediaType, jsonBody)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return httpClient.newCall(request).execute()
    }
}

class RouteMonitoringGetter(
    val apiKey: String,
) {
    private val httpClient = OkHttpClient()
    private val httpRequest = OkHttpRequest(httpClient)

    fun createRoute(name: String, waypoints: List<GeoPoint>): Response {
        val waypointsJsonArray = waypoints.map { it.toJsonObject() }.joinToString(prefix = "[", postfix = "]")
        val jsonBody = """{"name": "$name", "pathPoints": $waypointsJsonArray}"""
        val url = "https://api.tomtom.com/routemonitoring/3/routes?key=${apiKey}"

        return httpRequest.post(url = url, jsonBody = jsonBody)
    }

    fun listAllRoutes(): Response {
        val url = "https://api.tomtom.com/routemonitoring/3/routes?key=${apiKey}"
        return httpRequest.GET(url = url, callback = object: Callback {
            override fun onResponse(call: Call, response: Response) {
                println("All routes: ${response.body()}")
            }
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to get all routs: $e")
            }
        })
    }

    fun getRouteUpdate(routeId: RouteId): Response {
        val url = "https://api.tomtom.com/routemonitoring/3/routes/${routeId.uniqueId.id}?key=${apiKey}"
        return httpRequest.GET(url = url, callback = object: Callback {
            override fun onResponse(call: Call, response: Response) {
                println("Found route with routeId: ${routeId.uniqueId.id} - ${response.body()}")
            }
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to fetch route with routeId: ${routeId.uniqueId.id} - $e")
            }
        })
    }

    private fun GeoPoint.toJsonObject() = buildJsonObject {
        put("latitude", latitude)
        put("longitude", longitude)
    }
}
