package cr.ac.una.controlfinancierocameraAndres.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import cr.ac.una.controlfinancierocameraAndres.R
import cr.ac.una.controlfinancierocameraAndres.clases.page
import cr.ac.una.controlfinancierocameraAndres.VistaWeb

class BuscadorAdapter(context: Context, pages: List<page>) :
    ArrayAdapter<page>(context, 0, pages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_busqueda, parent, false)

        val title = view.findViewById<TextView>(R.id.titleView)
        val extract = view.findViewById<TextView>(R.id.extractView)
        val imageView = view.findViewById<ImageView>(R.id.image_view)

        val pageItem = getItem(position)

        title.text = pageItem?.titles?.normalized ?: "Sin título"

        val extractText = pageItem?.extract ?: "Sin extracto"
        extract.text = if (extractText.length > 300) extractText.substring(0, 300) + "..." else extractText

        pageItem?.thumbnail?.source?.let { url ->
            Glide.with(context)
                .load(url)
                .into(imageView)
        }

        view.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("page", pageItem)
                putString("url", "https://es.wikipedia.org/wiki/${pageItem?.title}") // Construye la URL del artículo de la lista Wiki
            }
            val fragment = VistaWeb().apply {
                arguments = bundle
            }
            (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.home_content, fragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}