package edu.vt.cs.cs5254.answerbutton

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.security.AccessControlContext

private const val EXTRA_TOTAL_CORRECT = "total_correct"

class Results : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val correctAnswerCount = intent.getIntExtra(
            EXTRA_TOTAL_CORRECT, -1)
//        val correctAnswerText = getString(R.string.correct_answers, correctAnswerCount)
//        ui.correctAnswers.setText(correctAnswerText)
    }
//
//    companion object {
//        fun newIntent(packageContext: Context, totalCorrect: Int): Intent {
//            return Intent(packageContext, Results::class.java).apply {
//                putExtra(EXTRA_TOTAL_CORRECT, totalCorrect)
//            }
//        }
//    }


}