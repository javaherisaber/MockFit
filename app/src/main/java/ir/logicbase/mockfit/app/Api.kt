@file:Suppress("HardCodedStringLiteral")

package ir.logicbase.mockfit.app

import ir.logicbase.mockfit.Mock
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    @Mock("picsum_list.json", excludeQueries = ["type={#}"])
    @GET("list")
    fun getListOfPicsums(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Call<List<Picsum>>

    @Mock("picsum_favorites.json", includeQueries = ["type=favorites"])
    @GET("list")
    fun getFavoritePicsums(
        @Query("type") type: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Call<List<Picsum>>
}