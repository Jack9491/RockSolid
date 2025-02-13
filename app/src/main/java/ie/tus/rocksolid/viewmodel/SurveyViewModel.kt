package ie.tus.rocksolid.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SurveyViewModel : ViewModel() {

    // Holds the user's responses as a map of questionIndex to response
    private val _responses = MutableStateFlow<Map<Int, String>>(emptyMap())
    val responses: StateFlow<Map<Int, String>> = _responses

    // Save a response for a specific question
    fun saveResponse(questionIndex: Int, response: String) {
        _responses.value = _responses.value.toMutableMap().apply {
            this[questionIndex] = response
        }
    }

    // Retrieve a response for a specific question
    fun getResponse(questionIndex: Int): String? {
        return _responses.value[questionIndex]
    }
}
