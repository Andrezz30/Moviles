package cr.ac.una.controlfinancierocamera

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import retrofit2.HttpException
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cr.ac.una.controlfinancierocamera.adapter.BuscadorAdapter
import cr.ac.una.controlfinancierocamera.clases.page
import cr.ac.una.controlfinancierocamera.controller.PageController
import cr.ac.una.controlfinancierocamera.service.LocationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

class ListControlFinancieroFragment : Fragment() {

    private lateinit var buscadorAdapter: BuscadorAdapter
    val pageController = PageController();
    private lateinit var botonBuscar: Button
    private lateinit var buscarNotificacion : Button
    private lateinit var buscadorView: SearchView
    var textoBusqueda = ""
    private var receiver: BroadcastReceiver? = null

    private fun startLocationService() {
        val serviceIntent = Intent(requireContext(), LocationService::class.java)
        ContextCompat.startForegroundService(requireContext(), serviceIntent)
    }
    private fun stopLocationService() {
        val serviceIntent = Intent(requireContext(), LocationService::class.java)
        requireContext().stopService(serviceIntent)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver!!)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* Registrar el BroadcastReceiver */
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val placeName = intent?.getStringExtra("placeName")
                    if (placeName != null) {
                        textoBusqueda = placeName
                    }
                }
            },
            IntentFilter("placeNameBroadcast")
        )
        //stopLocationService()
        startLocationService()

        return inflater.inflate(R.layout.fragment_list_control_financiero, container, false)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //cambio
        super.onViewCreated(view, savedInstanceState)

        botonBuscar = view.findViewById<Button>(R.id.buscar)
        buscadorView = view.findViewById(R.id.buscador)
        buscarNotificacion = view.findViewById(R.id.buscarNotif)
        var texto1 = view.findViewById<TextView>(R.id.textView)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val placeName = intent?.getStringExtra("placeName")
                if (placeName != null) {
                    textoBusqueda = placeName
                }
            }
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            receiver!!,
            IntentFilter("placeNameBroadcast")
        )


        buscarNotificacion.setOnClickListener{

            texto1.setText(textoBusqueda)
            textoBusqueda = textoBusqueda.replace(" ", "_")
            Log.d("TextoBusqueda", textoBusqueda)
            insertEntity(textoBusqueda)
        }

        botonBuscar.setOnClickListener {
            var textoBusqueda = buscadorView.query.toString()
            textoBusqueda = textoBusqueda.replace(" ", "_")
            Log.d("TextoBusqueda", textoBusqueda)
            insertEntity(textoBusqueda)
        }

        val listView: ListView = view.findViewById(R.id.listaWiki)
        buscadorAdapter = BuscadorAdapter(requireContext(), mutableListOf())
        listView.adapter = buscadorAdapter

        listView.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = buscadorAdapter.getItem(position) as page
            val bundle = Bundle()
            bundle.putString("url", selectedItem.url)
            val fragment = VistaWeb()
            fragment.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.home_content, fragment)
                .addToBackStack(null)
                .commit()
        }

    }

    private fun insertEntity(textoBusqueda: String) {
        lifecycleScope.launch {
            try {
                val resultadoBusqueda = withContext(Dispatchers.IO) {
                    pageController.Buscar(textoBusqueda)
                }
                withContext(Dispatchers.Main) {
                    Log.d("Resultado de la busqueda:0", resultadoBusqueda.toString())
                    buscadorAdapter.clear()
                    buscadorAdapter.addAll(resultadoBusqueda)
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Log.e("HTTP_ERROR", "Error: ${e.message}")
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ERROR", "Error: ${e.message}")
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}