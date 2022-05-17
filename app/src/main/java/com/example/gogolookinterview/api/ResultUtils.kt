package com.example.gogolookinterview.api

import retrofit2.Response


fun <T : Any, R> Response<T>.getResult(mapping: (T) -> R): Result<R> {
    return if (isSuccessful) {
        val body = body()
        if (body != null) {
            Result.Success(mapping(body))
        } else {
            Result.Error(ServerErrorException(code().toString(), "Unexpected response body: null"))
        }
    } else {
        Result.Error(ServerErrorException(code().toString(), errorBody().toString()))
    }
}

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

class ServerErrorException(val errorCode: String?, message: String?): Exception(message)