package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.ViewModel
import java.util.*

class DreamListViewModel : ViewModel() {
    private val dreamRepository = DreamRepository.get()
    val dreamListLiveData = dreamRepository.getDreams()

    fun addDreamEntries(dreamWithEntries: DreamWithEntries){
        dreamWithEntries.dreamEntries += DreamEntry(dreamId = dreamWithEntries.dream.id, kind = DreamEntryKind.CONCEIVED)
        dreamRepository.addDreamWithEntries(dreamWithEntries)
    }

    fun deleteAllDreams() {
        dreamRepository.deleteAllDreamsInDatabase()
    }

}

