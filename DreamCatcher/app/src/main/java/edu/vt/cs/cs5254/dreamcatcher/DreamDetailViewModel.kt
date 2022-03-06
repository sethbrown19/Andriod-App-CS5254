package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.ViewModel
import java.lang.IllegalArgumentException
import java.util.*

class DreamDetailViewModel : ViewModel() {

    private val dreamRepository = DreamRepository.get()
    lateinit var dream: Dream
    lateinit var dreamWithEntries: DreamWithEntries

    fun loadDream(dreamId: UUID) {
        dreamWithEntries = dreamRepository.getDreamWithEntries(dreamId) ?: throw IllegalArgumentException("Dream $dreamId not found")
        dream = dreamWithEntries.dream
    }





}