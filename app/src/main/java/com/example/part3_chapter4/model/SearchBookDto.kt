package com.example.part3_chapter4.model

import com.google.gson.annotations.SerializedName

data class SearchBookDto(
    @SerializedName("title") val title:String,
    @SerializedName("item") val books:List<Book>
)
