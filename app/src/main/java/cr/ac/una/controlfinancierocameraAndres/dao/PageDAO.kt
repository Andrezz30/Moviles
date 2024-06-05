package cr.ac.una.controlfinancierocameraAndres.dao

import cr.ac.una.controlfinancierocameraAndres.clases.pages
import retrofit2.http.GET
import retrofit2.http.Path

interface PageDAO {
    @GET("page/related/{title}")
    suspend fun Buscar(@Path("title") title: String): pages
}