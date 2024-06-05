package cr.ac.una.controlfinancierocamera.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
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
import cr.ac.una.controlfinancierocamera.R
var auxName = ""
var auxProb = 0
class LocationService : Service() {


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationManager: NotificationManager
    private var contNotificacion = 2

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
            }
        }
    }

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
                    val placeName = placeLikelihood.place.name?.toString()
                    val placePro = placeLikelihood.likelihood.toInt()
                    if(auxName!= placeName || auxProb > placePro) {
                        sendNotification("Lugar: $placeName, Probabilidad: ${placeLikelihood.likelihood}")
                        println("Lugar: $placeName, Probabilidad: ${placeLikelihood.likelihood}")
                        if (placeName != null) {
                            auxName=placeName
                        }
                        auxProb=placePro

                        // Enviar el broadcast

                        val intent = Intent("placeNameBroadcast")
                        intent.putExtra("placeName", placeName)
                        try {
                            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                        } catch (e: Exception) {
                            Log.e("BroadcastError", "Error al enviar el broadcast: ${e.message}")
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

    private fun sendNotification(message: String) {
        contNotificacion++
        val notification = NotificationCompat.Builder(this, "locationServiceChannel")
            .setContentTitle("Lugar encontrado")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        notificationManager.notify(contNotificacion, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
