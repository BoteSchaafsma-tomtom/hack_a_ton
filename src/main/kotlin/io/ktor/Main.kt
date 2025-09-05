package io.ktor

import com.urbanairship.api.client.UrbanAirshipClient
import com.urbanairship.api.push.PushRequest
import com.urbanairship.api.push.model.DeviceType
import com.urbanairship.api.push.model.DeviceType.ANDROID
import com.urbanairship.api.push.model.DeviceTypeData
import com.urbanairship.api.push.model.PushPayload
import com.urbanairship.api.push.model.audience.Selectors
import com.urbanairship.api.push.model.notification.Notification
import com.urbanairship.api.push.model.notification.android.AndroidDevicePayload
import com.urbanairship.api.push.model.notification.android.BigPictureStyle
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.User
import org.kodein.di.DI

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
    routing {
        post("/user") {
            val user = call.receive<User>()
            println(user)
            // Validate APID before sending notification
            call.respond("User received: ${user.airshipId}")
            sendTestNotification(user.airshipId)
        }
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

