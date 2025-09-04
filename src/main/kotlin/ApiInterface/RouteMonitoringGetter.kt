package ApiInterface

import kotlinx.io.IOException
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import libs.GeoPoint
import libs.RouteId
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class OkHttpRequest(
    private val httpClient: OkHttpClient,
) {
    fun GET(url: String, callback: Callback): Call {
        val request = Request.Builder().url(url).build()
        val call = httpClient.newCall(request)
        call.enqueue(callback)
        return call
    }

    fun POST(url: String, parameters: HashMap<String, String>, callback: Callback): Call {
        val formBuilder = FormBody.Builder()
        parameters.entries.forEach { formBuilder.add(it.key, it.value) }
        val formBody = formBuilder.build()
        val request = Request.Builder().url(url).post(formBody).build()

        val call = httpClient.newCall(request)
        call.enqueue(callback)
        return call
    }
}

class RouteMonitoringGetter(
    val apiKey: String,
) {
    private val httpClient = OkHttpClient()
    private val httpRequest = OkHttpRequest(httpClient)

    fun createRoute(name: String, waypoints: List<GeoPoint>): Response {
        val waypointsJsonArray = buildJsonArray {
            for (it in waypoints) add(it.toJsonObject())
        }
        val parameterHashMap = hashMapOf(
            "name" to name,
            "pathPoints" to waypointsJsonArray.toString(),
        )
        val url = "https://api.tomtom.com/routemonitoring/3/routes?key=${apiKey}"

        return httpRequest.POST(url = url, parameters = parameterHashMap, callback = object: Callback {
            override fun onResponse(call: Call, response: Response) {
                println("We have a response: ${response.body()}")
            }
            override fun onFailure(call: Call, e: IOException) {
                println("We have a failure: $e")
            }
        }).execute()
    }

    fun listAllRoutes(): Response {
        val url = "https//api.tomtom.com/routemonitoring/3/routes?key=${apiKey}"
        return httpRequest.GET(url = url, callback = object: Callback {
            override fun onResponse(call: Call, response: Response) {
                println("All routes: ${response.body()}")
            }
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to get all routs: $e")
            }
        }).execute()
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
        }).execute()
    }

    private fun GeoPoint.toJsonObject() = buildJsonObject {
        put("latitude", latitude)
        put("longitude", longitude)
    }
}
