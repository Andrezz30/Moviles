package cr.ac.una.controlfinancierocamera.controller

import cr.ac.una.controlfinancierocamera.clases.page
import cr.ac.una.controlfinancierocamera.service.PagesService

class PageController {
    var pagesService = PagesService()

    suspend fun  Buscar(terminoBusqueda: String):ArrayList<page>{
        return pagesService.apiWikiService.Buscar(terminoBusqueda).pages as ArrayList<page>
    }
}