package com.test.coinapp.data

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiServiceProvider {
    private var coinApiService: CoinApiService? = null
    private var coinApiKey: String? = null

    //Setup client
    suspend fun getServiceAsync() : CoinApiService? {
        return getCoinApiKeySync()?.let {
            getServiceForApiKey(it)
        }
    }

    fun getService(onComplete: (CoinApiService)->Unit, onFailed: (errorCode: Int) -> Unit) {
        requestCoinApiKey(
            onApiKeyAcquired = { key ->
                onComplete(getServiceForApiKey(key))
            },
            onFailed = onFailed
        )
    }

    private fun getServiceForApiKey(key: String) : CoinApiService {
        return synchronized(this) {
            val currentService = coinApiService
            val service = if (key != coinApiKey || currentService == null) {
                coinApiKey = key
                getNewCoinApiService(key)
            } else {
                currentService
            }
            coinApiService = service
            service
        }
    }

    /**
     * Запрос на получение от backend api-ключа для coinapi.
     * В данном случае просто подставляем значение из константы
     */
    private suspend fun getCoinApiKeySync() : String? {
        return coinApiKey ?: COIN_API_KEY
    }

    private fun requestCoinApiKey(onApiKeyAcquired: (apiKey: String)->Unit, onFailed: (errorCode: Int)->Unit) {
        coinApiKey?.also {
            onApiKeyAcquired(it)
            return
        }
        //fetching key...
        onApiKeyAcquired(COIN_API_KEY)
    }

    private fun getNewCoinApiService(apiKey: String) : CoinApiService {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .client(getClient(apiKey))
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return retrofit.create(CoinApiService::class.java)
    }

    private fun getClient(apiKey: String?) : OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(RequestInterceptor(apiKey))
            .build()
    }

    private class RequestInterceptor(val apiKey: String?) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val original: Request = chain.request()
            val builder = original.newBuilder()
                .addHeader(HEADER_KEY_ACCEPT, HEADER_VALUE_ACCEPT)
                .addHeader(HEADER_KEY_ACCEPT_ENCODING, HEADER_VALUE_ACCEPT_ENCODING)
                .method(original.method(), original.body())
            apiKey?.also {
                builder.addHeader(HEADER_KEY_API_KEY, it)
            }
            val request: Request = builder.build()
            return chain.proceed(request)
        }
    }

    companion object {
        const val RESULT_CODE_API_KEY_ERROR = -1
        private const val BASE_URL = "https://rest.coinapi.io/v1/"
        private const val COIN_API_KEY = "CB8115B1-CB5B-429F-9942-D8364EC6627F"
        private const val HEADER_KEY_ACCEPT = "Accept"
        private const val HEADER_VALUE_ACCEPT = "application/json"
        private const val HEADER_KEY_ACCEPT_ENCODING = "Accept-Encoding"
        private const val HEADER_VALUE_ACCEPT_ENCODING = "deflate"
        private const val HEADER_KEY_API_KEY = "X-CoinAPI-Key"
    }
}