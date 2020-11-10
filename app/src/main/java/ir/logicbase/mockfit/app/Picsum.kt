@file:Suppress("HardCodedStringLiteral")

package ir.logicbase.mockfit.app

import com.google.gson.annotations.SerializedName

data class Picsum(
    @field:SerializedName("id")
    val id: Int,
    @field:SerializedName("author")
    val author: String,
    @field:SerializedName("width")
    val width: Int,
    @field:SerializedName("height")
    val height: Int,
    @field:SerializedName("url")
    val url: String,
    @field:SerializedName("download_url")
    val downloadUrl: String
)