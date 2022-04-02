package edu.vt.cs.cs5254.dreamcatcher

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import edu.vt.cs.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding
import java.util.*

private const val TAG = "DreamDetailFragment"
private const val ARG_DREAM_ID = "dream_id"
const val REQUEST_KEY_ADD_REFLECTION = "request_key"
const val BUNDLE_KEY_REFLECTION_TEXT = "bundle_key"

class DreamDetailFragment : Fragment() {

    private var _binding: FragmentDreamDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var dreamWithEntries: DreamWithEntries
    private val viewModel: DreamDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dreamWithEntries = DreamWithEntries(Dream(), listOf())
        val dreamId: UUID = arguments?.getSerializable(ARG_DREAM_ID) as UUID
        Log.d(TAG, "Dream detail fragment for dream with ID ${dreamWithEntries.dream.id}")
        viewModel.loadDream(dreamId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.dreamLiveData.observe(
            viewLifecycleOwner,
            Observer { dreamWithEntries ->
                dreamWithEntries?.let {
                    this.dreamWithEntries = dreamWithEntries
                    updateUI()
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDreamDetailBinding.inflate(layoutInflater, container, false)

        // initialize view-binding
        val view = binding.root
        return view
    }


    override fun onStart() {
        super.onStart()
        binding.dreamTitleText.doOnTextChanged { text, start, before, count ->
            dreamWithEntries.dream.title = text.toString()

        }
        binding.dreamFulfilledCheckbox.setOnClickListener {
            onFulfilledClick()
        }

        binding.dreamDeferredCheckbox.setOnClickListener {
            onDeferredClick()
        }
        binding.addReflectionButton.setOnClickListener {
            AddReflectionDialog().show(parentFragmentManager, REQUEST_KEY_ADD_REFLECTION)
        }

        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY_ADD_REFLECTION, viewLifecycleOwner
        )
        { _, bundle ->
            var reflectionText = bundle.getString(BUNDLE_KEY_REFLECTION_TEXT, "")
            val newDreamEntry = DreamEntry(
                dreamId = dreamWithEntries.dream.id,
                text = reflectionText,
                kind = DreamEntryKind.REFLECTION
            )
            dreamWithEntries.dreamEntries += newDreamEntry
            updateUI()
        }



    }


    override fun onStop() {
        super.onStop()
        viewModel.saveDreamWithEntries(dreamWithEntries)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI() {
        binding.dreamTitleText.setText(dreamWithEntries.dream.title)
        updateUICheckbox()
        updateUIButtons()
    }

    private fun onFulfilledClick() {
        if (binding.dreamFulfilledCheckbox.isChecked) {
            dreamWithEntries.dream.isFulfilled = true
            dreamWithEntries.dream.isDeferred = false
            val newDreamEntry = DreamEntry(
                dreamId = dreamWithEntries.dream.id,
                date = Date(),
                text = "",
                kind = DreamEntryKind.FULFILLED,
                id = UUID.randomUUID()
            )
            dreamWithEntries.dreamEntries += newDreamEntry
            updateUI()
        } else {
            dreamWithEntries.dream.isFulfilled = false
            dreamWithEntries.dream.isDeferred = false
            dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries.filter { dreamEntry -> dreamEntry.kind != DreamEntryKind.FULFILLED }

//            dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries.dropLast(1)
            updateUI()
        }
    }

    private fun onDeferredClick() {
        if (binding.dreamDeferredCheckbox.isChecked) {
            dreamWithEntries.dream.isFulfilled = false
            dreamWithEntries.dream.isDeferred = true
            val newDreamEntry = DreamEntry(
                dreamId = dreamWithEntries.dream.id,
                date = Date(),
                text = "",
                kind = DreamEntryKind.DEFERRED,
                id = UUID.randomUUID()
            )
            dreamWithEntries.dreamEntries += newDreamEntry
            updateUI()
        } else {
            dreamWithEntries.dream.isFulfilled = false
            dreamWithEntries.dream.isDeferred = false
//            dreamWithEntries.dreamEntries =
//                dreamWithEntries.dreamEntries.dropLast(1)
            dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries.filter { dreamEntry -> dreamEntry.kind != DreamEntryKind.DEFERRED }

            updateUI()
        }
    }

    private fun updateUICheckbox() {
        when {
            dreamWithEntries.dream.isDeferred -> {
                binding.dreamDeferredCheckbox.isChecked = true
                binding.dreamFulfilledCheckbox.isChecked = false
                binding.dreamDeferredCheckbox.isEnabled = true
                binding.dreamFulfilledCheckbox.isEnabled = false
                binding.addReflectionButton.isEnabled = true
                binding.dreamDeferredCheckbox.jumpDrawablesToCurrentState()
            }
            dreamWithEntries.dream.isFulfilled -> {
                binding.dreamDeferredCheckbox.isChecked = false
                binding.dreamFulfilledCheckbox.isChecked = true
                binding.dreamDeferredCheckbox.isEnabled = false
                binding.dreamFulfilledCheckbox.isEnabled = true
                binding.addReflectionButton.isEnabled = false
                binding.dreamFulfilledCheckbox.jumpDrawablesToCurrentState()
            }
            else -> {
                binding.dreamDeferredCheckbox.isEnabled = true
                binding.dreamFulfilledCheckbox.isEnabled = true
                binding.dreamDeferredCheckbox.isChecked = false
                binding.dreamFulfilledCheckbox.isChecked = false
                binding.addReflectionButton.isEnabled = true

            }
        }
    }

    private fun updateUIButtons() {
        var dreamEntryList = dreamWithEntries.dreamEntries
        val entryButtonList = listOf(
            binding.dreamEntry0Button,
            binding.dreamEntry1Button,
            binding.dreamEntry2Button,
            binding.dreamEntry3Button,
            binding.dreamEntry4Button,
        )
        entryButtonList.forEach { button ->
            button.visibility = View.GONE
        }

        var buttonKindPair = dreamEntryList.zip(entryButtonList)
        buttonKindPair.forEach { (dreamEntry, button) ->
            if (dreamEntry.kind == DreamEntryKind.CONCEIVED) {
                button.text = dreamEntry.kind.toString()
                button.visibility = View.VISIBLE
                button.isEnabled = false
                val buttonColor = Color.parseColor("#0B0FDF")
                button.backgroundTintList = ColorStateList.valueOf(buttonColor)
            } else if (dreamEntry.kind == DreamEntryKind.REFLECTION) {
                val df = DateFormat.getMediumDateFormat(context)
                val formattedDate = df.format(dreamEntry.date)
                button.text = formattedDate.toString() + ": " + dreamEntry.text
                button.visibility = View.VISIBLE
                val buttonColor = Color.parseColor("#D61D66")
                button.backgroundTintList = ColorStateList.valueOf(buttonColor)
            } else if (dreamEntry.kind == DreamEntryKind.FULFILLED || dreamWithEntries.dream.isFulfilled) {
                button.text = "Fulfilled"
                button.visibility = View.VISIBLE
                val buttonColor = Color.parseColor("#4CAF50")
                button.backgroundTintList = ColorStateList.valueOf(buttonColor)
            } else if (dreamEntry.kind == DreamEntryKind.DEFERRED || dreamWithEntries.dream.isDeferred) {
                button.text = "Deferred"
                button.visibility = View.VISIBLE
                val buttonColor = Color.parseColor("#FFBB86FC")
                button.backgroundTintList = ColorStateList.valueOf(buttonColor)
            }
        }
    }

    companion object {
        fun newInstance(dreamId: UUID): DreamDetailFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DREAM_ID, dreamId)
            }
            return DreamDetailFragment().apply {
                arguments = args
            }
        }
    }
}
