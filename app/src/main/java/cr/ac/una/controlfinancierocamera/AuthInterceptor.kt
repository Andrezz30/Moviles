package cr.ac.una.controlfinancierocamera

import okhttp3.Interceptor
import okhttp3.Response


class AuthInterceptor(val apiKey: String): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $apiKey")
            .build()
        return chain.proceed(request)
    }
}