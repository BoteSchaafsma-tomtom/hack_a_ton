package io.ktor

import ApiInterface.RouteMonitoringGetter
import com.urbanairship.api.client.UrbanAirshipClient
import com.urbanairship.api.push.PushRequest
import com.urbanairship.api.push.model.DeviceType.ANDROID
import com.urbanairship.api.push.model.DeviceTypeData
import com.urbanairship.api.push.model.PushPayload
import com.urbanairship.api.push.model.audience.Selectors
import com.urbanairship.api.push.model.notification.Notification
import com.urbanairship.api.push.model.notification.android.AndroidDevicePayload
import com.urbanairship.api.push.model.notification.android.BigPictureStyle
import io.ktor.http.decodeURLPart
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import libs.CreateRouteParameters
import model.User
import org.jetbrains.kotlin.com.google.gson.Gson
import org.jetbrains.kotlin.com.google.gson.JsonParseException
import org.jetbrains.kotlin.com.google.gson.JsonSyntaxException
import org.kodein.di.DI

private val channelIdToRouteId = HashMap<String, Long>()

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        module = Application::myApp,
    ).start(wait = true)
}

/**
 * The main and only module of the application.
 */
fun Application.myApp() = myApplication(DI {
    // Dependencies Injection container
})

fun Application.myApplication(application: DI) {
    install(ContentNegotiation) {
        json()
    }
    val getter = RouteMonitoringGetter(apiKey = route_monitoring_api_key)

    routing {
        post("/user") {
            val user = call.receive<User>()
            println(user)
            // Validate APID before sending notification
            call.respond("User received: ${user.airshipId}")
            sendTestNotification(user.airshipId)
        }
        post(path = "/createRoute") { handleCreateRoute(call.receiveText(), getter) }
    }
}

private fun handleCreateRoute(receivedText: String, getter: RouteMonitoringGetter) {
    try {
        val jsonString = receivedText.substring(4).decodeURLPart()
        val routeParameters = Gson().fromJson(jsonString, CreateRouteParameters::class.java)
        val response = getter.createRoute(
            "home_work_combination",
            listOf(routeParameters.start, routeParameters.destination)
        )
        if (response.code() != 200) {
            println("Response failed with code: ${response.code()}")
            return
        }
        val matchResult = response.body()?.string()?.let { body ->
            val pattern = Regex("\"routeId\":([0-9]+),")
            return@let pattern.find(body)
        }
        val routeId = matchResult?.groupValues?.takeIf { it.isNotEmpty() }?.get(1)
        println("Created route with id '$routeId' and channel id '${routeParameters.channelId}'")
        routeId?.let { channelIdToRouteId[routeParameters.channelId] = it.toLong() }
    } catch (e: BadRequestException) {
        println("Error: bad request: $e")
    } catch (e: JsonParseException) {
        println("Error: json parse exception: $e")
    } catch (e: JsonSyntaxException) {
        println("Error: json syntax exception: $e")
    } catch (e: IllegalStateException) {
        println("Error: illegal state exception: $e")
    }

}

val client: UrbanAirshipClient? by lazy {
    UrbanAirshipClient
        .newBuilder()
        .setKey(airship_app_key)
        .setSecret(airship_secret)
        .setUseEuropeanSite(true)
        .build()
}

// Make this function suspend and return a result string
private suspend fun sendTestNotification(id: String): String =
    withContext(Dispatchers.IO) {
        // Build BigPictureStyle with the image URL
        val bigPictureStyle = BigPictureStyle
            .newBuilder()
            .setContent("https://lh3.googleusercontent.com/pw/AP1GczNAFJzH4dhcJ5-Tb0dBZb9iSMY5-LquVNAzY4BA5-_DCXuZhENX6kHC2lDv2nFAYwY3JpStgAUJ0hADlf_T_cdmt95QljSkSbof-e4sHDt_Q1AmbeZ8PwpJpx7AGKZToDNuGU-quLtVUw8AwUVAN7Q=w640-h276-s-no-gm?authuser=0")
            .build()

        val androidNotification = AndroidDevicePayload
            .newBuilder()
            .setAlert("Your upcoming trip")
            .setStyle(bigPictureStyle)
            .build()

        val notification = Notification
            .newBuilder()
            .addDeviceTypeOverride(ANDROID, androidNotification)
            .build()

        val payload = PushPayload
            .newBuilder()
            .setAudience(Selectors.androidChannel(id))
            .setNotification(notification)
            .setDeviceTypes(DeviceTypeData.of(ANDROID))
            .build()

        val request = PushRequest.newRequest(payload)

        return@withContext try {
            val response = client?.execute(request)
            "Response: $response"
        } catch (e: Exception) {
            // Print stack trace for debugging
            e.printStackTrace()
            "Exception in API request: ${e.message}"
        }
    }

