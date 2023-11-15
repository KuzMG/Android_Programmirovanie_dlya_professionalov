package com.example.myeducation

import androidx.lifecycle.ViewModel

private const val TAG = "QuizViewModel"

class QuizViewModel : ViewModel() {
    private val questionBank = listOf(
        Question(R.string.question_capital, true),
        Question(R.string.question_continent, false),
        Question(R.string.question_ocean, true),
        Question(R.string.question_russia, true)
    )
    var isCheater = false
    var cheatCount = 0
    var currentIndex = 0
        get() = field
        set(value){
            if(value!=field)
                field=value
        }
    val currentQuestionAnswer: Boolean
        get() = questionBank[currentIndex].answer
    val currentQuestionText: Int
        get() = questionBank[currentIndex].textResId

    fun moveToNext() {
        currentIndex = (currentIndex + 1) % questionBank.size
    }
}