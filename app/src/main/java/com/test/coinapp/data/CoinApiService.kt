package com.test.coinapp.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CoinApiService {

    @GET(METHOD_ASSETS)
    suspend fun getCurrenciesAsync() : Response<List<CurrencyAsset>>

    @GET("$METHOD_EXCHANGE_RATE/{$PARAM_ASSET_ID_BASE}")
    suspend fun getRatesAsync(@Path(PARAM_ASSET_ID_BASE) currencyId: String) : Response<Rates>

    @GET("$METHOD_EXCHANGE_RATE/{$PARAM_ASSET_ID_BASE}/{$PARAM_ASSET_ID_QUOTE}")
    suspend fun getExchangeRateAsync(
        @Path(PARAM_ASSET_ID_BASE) baseCurrencyId: String,
        @Path(PARAM_ASSET_ID_QUOTE) targetCurrencyId: String
    ) : Response<SingleRate>

    companion object {
        const val METHOD_ASSETS = "assets"
        const val METHOD_EXCHANGE_RATE = "exchangerate"
        const val PARAM_ASSET_ID_BASE = "asset_id_base"
        const val PARAM_ASSET_ID_QUOTE = "asset_id_quote"
    }
}

data class CurrencyAsset(
    @SerializedName("asset_id")
    val assetId : String?,
    val name : String?,
    @SerializedName("type_is_crypto")
    val typeIsCrypto : Int?,
    @SerializedName("price_usd")
    val priceUSD : Double?
)

data class Rates(
    @SerializedName("asset_id_base")
    val assetIdBase : String?,
    val rates : List<Rate>?
) {
    data class Rate (
        val time : String?,
        @SerializedName("asset_id_quote")
        val assetIdQuote : String?,
        val rate : Double?
    )
}

data class SingleRate(
    val time : String?,
    @SerializedName("asset_id_base")
    val assetIdBase: String?,
    @SerializedName("asset_id_quote")
    val assetIdQuote : String?,
    val rate : Double?
)