package no.exotech.pricefetcher.apis

import com.google.gson.Gson
import no.exotech.pricefetcher.common.RequestBuilder
import no.exotech.pricefetcher.common.RequestValues
import no.exotech.pricefetcher.apis.stores.Meny
import no.exotech.pricefetcher.apis.stores.Store
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.stream.Collectors

fun main() {
    val response = Caller.call(Meny())
    print(response)
}


object Caller {
    @JvmStatic
    private val client = OkHttpClient().newBuilder().build()

    @JvmStatic
    fun <T : Any> call(store: Store<T>): List<T> {
        val responses = callAllPages(store.requestValues)
        return mapResponses(responses, store.responseClass)
    }

    @JvmStatic
    private fun callAllPages(requestValues: RequestValues): List<Response> {
        val requestBuilder = RequestBuilder.getRequestBuilder(requestValues)
        return requestValues.pages.parallelMap { page ->
            client.newCall(requestBuilder.withPage("$page")).execute()
        }
    }

    @JvmStatic
    private fun <T : Any> mapResponses(responses: List<Response>, clazz: Class<T>): List<T> {
        val gson = Gson()
        return responses.map { response ->
            val string = response.body?.string()

            gson.fromJson(string, clazz)
        }
    }

    @JvmStatic
    private fun <R, T> List<T>.parallelMap(mapper: (t: T) -> R): List<R> {
        return this
            .parallelStream()
            .map { mapper(it) }
            .collect(Collectors.toList())
    }
}