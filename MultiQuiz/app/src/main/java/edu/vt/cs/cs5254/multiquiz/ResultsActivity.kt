package edu.vt.cs.cs5254.multiquiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import multiquiz.databinding.ActivityResultsBinding

private const val EXTRA_TOTAL_CORRECT = "total_correct"

class ResultsActivity : AppCompatActivity() {
    private lateinit var  ui : ActivityResultsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //
        //Create binding and content view
        //
        ui = ActivityResultsBinding.inflate(layoutInflater)
        val view = ui.root
        setContentView(view)
        //
        //Set Correct answer text
        //
        val correctAnswerCount = intent.getIntExtra(
            EXTRA_TOTAL_CORRECT, -1)
//        val correctAnswerText = getString(R.string.correct_answers, correctAnswerCount)
//        ui.correctAnswers.setText(correctAnswerText)
    }

//    companion object {
//        fun newIntent(packageContext: Context, totalCorrect: Int): Intent {
//            return Intent(packageContext, Results::class.java).apply {
//                putExtra(EXTRA_TOTAL_CORRECT, totalCorrect)
//            }
//        }
//    }


}