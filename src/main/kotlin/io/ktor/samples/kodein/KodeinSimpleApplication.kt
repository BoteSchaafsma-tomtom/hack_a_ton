package io.ktor.samples.kodein

import ApiInterface.RouteMonitoringGetter
import ApiInterface.hasAnomalies
import ApiInterface.toRouteStatus
import io.ktor.http.decodeURLPart
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receiveText
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import libs.CreateRouteParameters
import org.jetbrains.kotlin.com.google.gson.Gson
import org.jetbrains.kotlin.com.google.gson.JsonParseException
import org.jetbrains.kotlin.com.google.gson.JsonSyntaxException
import org.kodein.di.DI
import java.util.*
import kotlin.collections.HashMap
import kotlin.time.Duration.Companion.seconds

private val channelIdToRouteId = HashMap<String, Long>()

/**
 * An entry point of the embedded-server program:
 *
 * io.ktor.samples.kodein.KodeinSimpleApplicationKt.main
 *
 * This would start and wait a web-server at port 8080 using Netty,
 * and would load the 'myKodeinApp' ktor module.
 */
fun main() {
    embeddedServer(Netty, port = 8080, module = Application::myKodeinApp).start(wait = true)
}

fun sendToAirship(routeId: Long) {
    println("Sending to Airship: $routeId")
}

/**
 * The main and only module of the application.
 * This module creates a Kodein container and sets
 * maps a Random to a singleton based on SecureRandom.
 * And then configures the application.
 */
fun Application.myKodeinApp() = myKodeinApp(DI { })

fun CoroutineScope.launchCronJobs(getter: RouteMonitoringGetter) {
    launch {
        while (true) {
            delay(20.seconds)
            val routeStatusList = getter.listAllRoutes().body()?.string()?.toRouteStatus()
            routeStatusList?.forEach {
                if (it.hasAnomalies()) {
                    sendToAirship(it.routeId)
                }
            }
        }
    }
}

/**
 * This is the application module that has a
 * preconfigured [kodein] instance as input.
 *
 * The idea of this method, is that the different modules
 * can call this with several configured kodein variants
 * and also you can call it from the tests setting mocks
 * instead of the default mappings.
 */
fun Application.myKodeinApp(kodein: DI) {
    val getter = RouteMonitoringGetter(apiKey = apiKey)
    routing {
        get("/listAll") {
            val response = getter.listAllRoutes().body()?.string()?.toRouteStatus()
            call.respondText(response.toString())
        }
        post(path = "/createRoute") {
            try {
                val jsonString = call.receiveText().substring(4).decodeURLPart()
                val routeParameters = Gson().fromJson(jsonString, CreateRouteParameters::class.java)
                val response = getter.createRoute(
                    "home_work_combination",
                    listOf(routeParameters.start, routeParameters.destination)
                )
                if (response.code() != 200) {
                    println("Response failed with code: ${response.code()}")
                    return@post
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
    }
}

/**
 * Convenience [Random] extension operator method to get a random integral value inside the specified [range].
 */
private operator fun Random.get(range: IntRange) = range.first + this.nextInt(range.last - range.first)
