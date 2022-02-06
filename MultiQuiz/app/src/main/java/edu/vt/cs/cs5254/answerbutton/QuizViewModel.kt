package edu.vt.cs.cs5254.answerbutton

import androidx.lifecycle.ViewModel

class QuizViewModel : ViewModel() {

    private val questionBank = listOf(
        Question(
            R.string.us_question,
            listOf(Answer(R.string.us_answer_dc, true),
                Answer(R.string.us_answer_desmoines, false),
                Answer(R.string.us_answer_dallas, false),
                Answer(R.string.us_answer_seattle, false)
            )
        ),
        Question(
            R.string.ocean_question,
            listOf(Answer(R.string.ocean_answer_arctic, false),
                Answer(R.string.ocean_answer_pacific, true),
                Answer(R.string.ocean_answer_indian, false),
                Answer(R.string.ocean_answer_atlantic, false)
            )
        ),
        Question(
            R.string.state_question,
            listOf(Answer(R.string.state_answer_blacksburg, false),
                Answer(R.string.state_answer_orland, false),
                Answer(R.string.state_answer_ny, true),
                Answer(R.string.state_answer_sf, false)
            )
        ),
        Question(
            R.string.president_question,
            listOf(Answer(R.string.pres_answer_goofy, false),
                Answer(R.string.pres_answer_kennedy, false),
                Answer(R.string.pres_answer_washington, false),
                Answer(R.string.pres_answer_biden, true)
            )
        )
    )

    private var questionIndex = 0

    val questionText
        get() = questionBank[questionIndex].textRedId
    val answerList
        get() = questionBank[questionIndex].answerList
    fun gotoNextQuestion() {
        questionIndex = (questionIndex + 1) % questionBank.size
    }


}