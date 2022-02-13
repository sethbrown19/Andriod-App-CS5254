package edu.vt.cs.cs5254.multiquiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import multiquiz.databinding.ActivityResultsBinding
import android.content.Context
import android.content.Intent


private const val EXTRA_TOTAL_CORRECT = "total_correct"
private const val EXTRA_TOTAL_QUESTIONS = "total_questions"
private const val EXTRA_HINTS_USED = "hint_used"


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
            EXTRA_TOTAL_CORRECT, -99)
        val questionTotal = intent.getIntExtra(EXTRA_TOTAL_QUESTIONS, -99)
        val hintsUsed = intent.getIntExtra(EXTRA_HINTS_USED, -99)

        ui.totalAnswersCorrectValue.text = correctAnswerCount.toString()
        ui.totalQuestionsValue.text = questionTotal.toString()
        ui.totalHintsUsedValue.text = hintsUsed.toString()



    }

    companion object {
        fun newIntent(packageContext: Context, totalQuestions: Int, hintsUsed: Int, totalCorrect: Int): Intent {
            return Intent(packageContext, ResultsActivity::class.java).apply {
                putExtra(EXTRA_TOTAL_QUESTIONS, totalQuestions)
                putExtra(EXTRA_HINTS_USED, hintsUsed)
                putExtra(EXTRA_TOTAL_CORRECT, totalCorrect)
            }
        }
    }


}