package cr.ac.una.controlfinancierocamera.service

import com.google.gson.GsonBuilder
import cr.ac.una.controlfinancierocamera.dao.PageDAO
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PagesService {

    val gson = GsonBuilder().setPrettyPrinting().create()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://es.wikipedia.org/api/rest_v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiWikiService = retrofit.create(PageDAO::class.java)
}