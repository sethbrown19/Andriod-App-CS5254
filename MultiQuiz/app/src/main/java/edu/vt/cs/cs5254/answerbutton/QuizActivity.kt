package edu.vt.cs.cs5254.answerbutton


import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import edu.vt.cs.cs5254.answerbutton.databinding.ActivityQuizBinding

class QuizActivity : AppCompatActivity() {

    private val DEFAULT_BUTTON_COLOR = "Purple"
    private val SELECTED_BUTTON_COLOR = "Green"
    private var hintPressedCount = 0

    lateinit var ui : ActivityQuizBinding

    // view fields (only one)
    lateinit var answerButtonList: List<Button>

    // view model access
    private val vm: QuizViewModel by lazy {
        ViewModelProvider(this).get(QuizViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ------------------------------------------------------
        // Create binding and content view
        // ------------------------------------------------------

        ui = ActivityQuizBinding.inflate(layoutInflater)
        val view = ui.root
        setContentView(view)

        // ------------------------------------------------------
        // Initialize answer-button list
        // ------------------------------------------------------

        answerButtonList = ui.answerButtons
            .children
            .toList()
            .filterIsInstance<Button>()

        // ------------------------------------------------------
        // Set text of views
        // ------------------------------------------------------

        ui.questionTextView.setText(R.string.us_question)

        answerButtonList.zip(vm.answerList).forEach {(button, answer) -> button.setText(answer.textResId)}



        ui.hintButton.setText(R.string.hint_button)
        ui.submitButton.setText(R.string.submit_button)
        updateQuestion()

//        // ------------------------------------------------------
//        // Add listeners to buttons
//        // ------------------------------------------------------
//
//        answerButtonList.zip(vm.answerList).forEach { (button, answer) ->
//            button.setOnClickListener {
//                processAnswerButtonClick(answer)
//            }
//        }
//
//
//        ui.hintButton.setOnClickListener {
//            processHintButtonClick()
//        }
//        ui.submitButton.setOnClickListener {
//            processSubmitButtonClick()
//        }

        // ------------------------------------------------------
        // Refresh the view
        // ------------------------------------------------------

        ui.submitButton.isEnabled = false
        refreshView()
    }

    private fun updateQuestion() {
        // ------------------------------------------------------
        // Add listeners to buttons
        // ------------------------------------------------------

        answerButtonList.zip(vm.answerList).forEach { (button, answer) ->
            button.setOnClickListener {
                processAnswerButtonClick(answer)
            }
        }

        ui.hintButton.setOnClickListener {
            processHintButtonClick()
            ui.submitButton.isEnabled = false
        }
        ui.submitButton.setOnClickListener {
            processSubmitButtonClick()
        }
    }

    private fun processAnswerButtonClick(clickedAnswer: Answer) {
        val origIsSelected = clickedAnswer.isSelected
        vm.answerList.forEach { answer -> !answer.isSelected }
        clickedAnswer.isSelected = !origIsSelected
        ui.submitButton.isEnabled = clickedAnswer.isSelected // enable submit if an answer is chosen

        refreshView()
    }

    private fun processHintButtonClick() {
        val randomAnswer = vm.answerList
            .filter { it.isEnabled }
            .filterNot { it.isCorrect}
            .random()

        randomAnswer.isEnabled = false
        randomAnswer.isSelected = false

        hintPressedCount += 1
        ui.hintButton.isEnabled = hintPressedCount < 3

        refreshView()
    }

    private fun processSubmitButtonClick() {
        vm.gotoNextQuestion()
        updateQuestion()

        for (answer in vm.answerList) {
            answer.isEnabled = true
            answer.isSelected = false
        }
        hintPressedCount = 0 // set count back to zero and enable hint button on page switch
        ui.hintButton.isEnabled = true

        refreshView()
    }



    private fun refreshView() {
        // ------------------------------------------------------
        // Set text of question and answer buttons
        // ------------------------------------------------------

        ui.questionTextView.setText(vm.questionText)

        answerButtonList.zip(vm.answerList).forEach {(button, answer) -> button.setText(answer.textResId)}


        answerButtonList.zip(vm.answerList).forEach { (button, answer) ->
            button.isEnabled = answer.isEnabled
            button.isSelected = answer.isSelected
            if (answer.isSelected) {
                setButtonColor(button, SELECTED_BUTTON_COLOR)
            } else {
                setButtonColor(button, DEFAULT_BUTTON_COLOR)
            }
            if (!answer.isEnabled) {
                button.alpha = .5f
            }
        }
    }

    private fun setButtonColor(button: Button, colorString: String) {
        button.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(colorString))
        button.setTextColor(Color.WHITE)
        button.alpha = 1f
    }
}
