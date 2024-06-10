package cr.ac.una.controlfinancierocamera.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import cr.ac.una.controlfinancierocamera.ListControlFinancieroFragment
import cr.ac.una.controlfinancierocamera.MainActivity
import cr.ac.una.controlfinancierocamera.R
import cr.ac.una.controlfinancierocamera.VistaWeb
import cr.ac.una.controlfinancierocamera.controller.PageController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import kotlin.properties.Delegates

var auxName = ""
var auxProb = 0
class LocationService : Service() {

    private var latitude by Delegates.notNull<Double>()
    private var longitude by Delegates.notNull<Double>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationManager: NotificationManager
    private lateinit var buttonPendingIntent: PendingIntent
    private lateinit var   buttonIntent :Intent
    private var contNotificacion = 2
    private var placeName: String? = null
    val pageController = PageController();

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        buttonIntent = Intent(this, VistaWeb::class.java).apply {
            action = "OPEN_FRAGMENT"
            putExtra("param_key", "param_value")
        }
        buttonPendingIntent = PendingIntent.getActivity(this, 0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        Places.initialize(applicationContext, "AIzaSyBLiFVeg7U_Ugu5bMf7EQ_TBEfPE3vOSF4")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        this.startForeground(1, createNotification("Service running"))

        requestLocationUpdates()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            "locationServiceChannel",
            "Location Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, "locationServiceChannel")
            .setContentTitle("Location Service")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 0L
        ).apply {
            setMinUpdateDistanceMeters(50.0f)
        }.build()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                getPlaceName(location.latitude, location.longitude)
                latitude = location.latitude
                longitude = location.longitude
            }
        }
    }

    /*@SuppressLint("MissingPermission")
    private fun getPlaceName(latitude: Double, longitude: Double) {
        val placeFields: List<Place.Field> = listOf(Place.Field.NAME)
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)
        val placesClient: PlacesClient = Places.createClient(this)
        var resultadoBusqueda = "Hola"
        val placeResponse = placesClient.findCurrentPlace(request)
        placeResponse.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                    val response = task.result
                    val topPlaces = response.placeLikelihoods
                        .sortedByDescending { it.likelihood }
                        .take(1)
                    topPlaces.forEach { placeLikelihood ->
                        placeName = placeLikelihood.place.name?.toString()
                        GlobalScope.launch(Dispatchers.Main) {
                            val textoBusqueda = placeName!!.replace(" ", "_")
                            if(auxName!= placeName) {
                                val resultadoBusqueda =
                                    if (!ListControlFinancieroFragment().busqueda(textoBusqueda)) "No encontrado"
                                    else "Encontrado"
                                sendNotification("Existe en Wikipedia: $resultadoBusqueda \nLatitud: $latitude \nLongitud: $longitude")
                                println("Existe en Wikipedia: $resultadoBusqueda \nLatitud: $latitude \nLongitud: $longitude")
                                auxName = placeName as String
                            }
                        }
                    }


                    } else {
                val exception = task.exception
                if (exception is ApiException) {
                    Log.e(ContentValues.TAG, "Lugar no encontrado: ${exception.statusCode}")
                }
            }
        }
    }
*/
    @SuppressLint("MissingPermission")
    private fun getPlaceName(latitude: Double, longitude: Double) {
        val placeFields: List<Place.Field> = listOf(Place.Field.NAME)
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)
        val placesClient: PlacesClient = Places.createClient(this)

        val placeResponse = placesClient.findCurrentPlace(request)
        placeResponse.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val response = task.result
                val topPlaces = response.placeLikelihoods
                    .sortedByDescending { it.likelihood }
                    .take(1)

                topPlaces.forEach { placeLikelihood ->
                    val placeName = placeLikelihood.place.name ?: "Unknown"
                    val message = "Lugar: $placeName, Probabilidad: ${placeLikelihood.likelihood}"
                    //sendNotification(message, placeName)
                    Log.d("LocationService", message)
                    // Call searchWikipediaAndNotify with the place name
                    searchWikipediaAndNotify(placeName)
                }
            } else {
                val exception = task.exception
                if (exception is ApiException) {
                    Log.e("LocationService", "Lugar no encontrado: ${exception.statusCode}")
                }
            }
        }
    }
    private fun sendNotification(message: String, wikipediaUrl: String) {
        val notificationId = contNotificacion++
        val intent = Intent(this, VistaWeb::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("url", wikipediaUrl)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            notificationId, // Use notificationId as the requestCode to ensure uniqueness
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "locationServiceChannel")
            .setContentTitle("Lugar encontrado")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private fun searchWikipediaAndNotify(placeName: String) {
        val formattedQuery = placeName.replace(" ", "_")
        serviceScope.launch {
            try {
                val resultadoBusqueda = withContext(Dispatchers.IO) {
                    pageController.Buscar(formattedQuery)
                }
                if (resultadoBusqueda.isNotEmpty()) {
                    val firstResultTitle = resultadoBusqueda.first().title
                    val wikipediaUrl = "https://es.wikipedia.org/wiki/$firstResultTitle"
                    sendNotification("Latitud: $latitude \nLongitud: $longitude", wikipediaUrl)
                    Log.d("ResultadoBusqueda", "Se encontró información: $resultadoBusqueda")
                } else {
                    Log.d("ResultadoBusqueda", "No se encontró información")
                }
            } catch (e: HttpException) {
                Log.e("HTTP_ERROR", "No se encontró información. Error: ${e.message}")
            } catch (e: Exception) {
                Log.e("ERROR", "No se encontró información. Error: ${e.message}")
            }
        }
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
