package com.dalamarmajere.dietrecomendation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Headers
import okio.IOException

data class OpenAIResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: MessageContent?
)

data class MessageContent(
    val content: String
)

class APIViewModel : ViewModel() {

    private val _responses = MutableStateFlow<List<String>>(emptyList())
    val responses = _responses.asStateFlow()

    private val client = OkHttpClient()

    // Your API key
    private val apiKey = "insert a token here"

    // Endpoint URL for the API
    private val url = "https://api.openai.com/v1/chat/completions"

    // MediaType for JSON
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun sendRequestToChatGPT(daysText: String, caloriesText: String, mealsText: String) {
        Log.e("MyViewModel","Sending request from sendRequestToChatGPT")
        viewModelScope.launch(Dispatchers.IO) {
            Log.e("MyViewModel","Sending request from viewModelScope")
            try {
                // Construct the message
                val message = "Please generate a diet for $daysText days and $caloriesText calories a day with $mealsText meals. " +
                        "It should be balanced and easy to make."

                // Prepare the JSON body of the request
                val requestBody = """
                    {
                        "model": "gpt-3.5-turbo",
                        "messages": [{"role": "user", "content": "$message"}]
                    }
                """.trimIndent().toRequestBody(mediaType)

                // Build the request
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .build()

                // Execute the request
                val response = client.newCall(request).execute()

                // Check if the response is successful
                if (response.isSuccessful) {
                    val responseString = response.body?.string()
                    response.body?.close() // Close the response body to free resources
                    val parsedResponse = parseResponse(responseString) // Parses the JSON response
                    viewModelScope.launch(Dispatchers.Main) {
                        _responses.value = parsedResponse
                        Log.e("MyViewModel", parsedResponse.toString())
                    }
                } else {
                    val errorBody = response.body?.string()
                    Log.e("MyViewModel", "Error: ${response.code} | Error Body: $errorBody")
                }
            } catch (e: IOException) {
                // Log or handle network error
                Log.e("MyViewModel", "Network error", e)
            }
        }
    }

    private fun parseResponse(responseString: String?): List<String> {
        if (responseString == null) return emptyList()
        val gson = Gson()
        val type = object : TypeToken<OpenAIResponse>() {}.type
        val response = gson.fromJson<OpenAIResponse>(responseString, type)
        return response.choices.mapNotNull { it.message?.content }
    }

    data class Response(val responses: List<String>)
}

