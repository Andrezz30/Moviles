package cr.ac.una.controlfinancierocameraAndres.controller

import cr.ac.una.controlfinancierocameraAndres.clases.page
import cr.ac.una.controlfinancierocameraAndres.service.PagesService

class PageController {
    var pagesService = PagesService()

    suspend fun  Buscar(terminoBusqueda: String):ArrayList<page>{
        return pagesService.apiWikiService.Buscar(terminoBusqueda).pages as ArrayList<page>
    }
}