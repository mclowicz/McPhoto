package com.mclowicz.mcphoto.data

sealed class Result {
    object Failure: Result()
    object Success: Result()
}