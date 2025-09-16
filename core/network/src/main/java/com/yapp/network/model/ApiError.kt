package com.yapp.network.model

data class ApiError(
    override val message: String,
) : Exception()
