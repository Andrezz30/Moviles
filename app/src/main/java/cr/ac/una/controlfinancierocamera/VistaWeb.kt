package cr.ac.una.controlfinancierocamera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast

class VistaWeb : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_vista_web)

        val webView = findViewById<WebView>(R.id.webView)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url ?: "")
                return true
            }
        }
        webView.settings.javaScriptEnabled = true

        val url = intent.getStringExtra("url")
        Log.d("WebViewActivity", "URL recibida: $url")

        if (!url.isNullOrBlank()) {
            webView.loadUrl(url)
        } else {
            Log.e("WebViewActivity", "URL es null o está vacía")
            Toast.makeText(this, "URL inválida. No se puede cargar la página.", Toast.LENGTH_SHORT).show()
        }

        val volverBoton = findViewById<Button>(R.id.volverBoton)
        volverBoton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}