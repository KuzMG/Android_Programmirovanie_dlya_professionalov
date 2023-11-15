package com.example.myeducation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

private const val EXTRA_ANSWER_IS_TRUE = "com.example.myeducation.geoquiz.answer_is_true"
const val EXTRA_ANSWER_SHOWN = "com.example.myeducation.geoquiz.answer_shown"

class CheatActivity : AppCompatActivity() {

    private var answerIsTrue = false
    private lateinit var answerTextView: TextView
    private lateinit var showAnswerButton: Button
    private lateinit var versionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cheat)
        answerIsTrue = intent.getBooleanExtra(EXTRA_ANSWER_IS_TRUE, false)
        answerTextView = findViewById(R.id.answer_text_view)
        showAnswerButton = findViewById(R.id.show_answer_button)
        versionTextView = findViewById(R.id.version_text_view)

        showAnswerButton.setOnClickListener {
            val answerText = when {
                answerIsTrue -> R.string.true_button
                else -> R.string.false_button
            }
            answerTextView.setText(answerText)
            setAnswerShownResult(true)
        }
        versionTextView.setText("API Level ${VERSION.SDK_INT}")
    }
    private fun setAnswerShownResult(isAnswer: Boolean) {
        val data = Intent().apply{
            putExtra(EXTRA_ANSWER_SHOWN,isAnswer)
        }
        setResult(Activity.RESULT_OK,data)
    }
    companion object {
        fun newIntent(packageContent: Context, answerIsTrue: Boolean): Intent =
            Intent(packageContent, CheatActivity::class.java).apply {
                putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue)
            }
    }
}