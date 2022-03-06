package edu.vt.cs.cs5254.dreamcatcher

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.vt.cs.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding
import java.util.*

private const val TAG = "DreamDetailFragment"
private const val ARG_DREAM_ID = "dream_id"

class DreamDetailFragment : Fragment() {

    private var _binding: FragmentDreamDetailBinding? = null
    private val binding get() = _binding!!


    private val viewModel: DreamDetailViewModel by lazy {
        ViewModelProvider(this).get(DreamDetailViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dreamId: UUID = arguments?.getSerializable(ARG_DREAM_ID) as UUID
        viewModel.loadDream(dreamId)
        Log.d(TAG, "Dream detail fragment for dream with ID ${viewModel.dream.id}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDreamDetailBinding.inflate(layoutInflater, container, false)

        // initialize view-binding
        val view = binding.root
        updateUI()
        return view
    }

    private fun updateUI() {
        binding.dreamTitleText.setText(viewModel.dream.title)
        updateUICheckbox()
        updateUIButtons()
    }

    private fun onFulfilledClick() {
        if (binding.dreamFulfilledCheckbox.isChecked) {
            viewModel.dreamWithEntries.dream.isFulfilled = true
            viewModel.dreamWithEntries.dream.isDeferred = false
            val newDreamEntry = DreamEntry(UUID.randomUUID(), viewModel.dream.date, "", DreamEntryKind.FULFILLED, UUID.randomUUID())
            viewModel.dreamWithEntries.dreamEntries += newDreamEntry
            updateUI()
        } else {
            viewModel.dreamWithEntries.dream.isFulfilled = false
            viewModel.dreamWithEntries.dream.isDeferred = false
            viewModel.dreamWithEntries.dreamEntries =
                viewModel.dreamWithEntries.dreamEntries.dropLast(1)
            updateUI()
        }
    }

    private fun onDeferredClick() {
        if (binding.dreamDeferredCheckbox.isChecked) {
            viewModel.dreamWithEntries.dream.isFulfilled = false
            viewModel.dreamWithEntries.dream.isDeferred = true
            val newDreamEntry = DreamEntry(UUID.randomUUID(), viewModel.dream.date, "", DreamEntryKind.DEFERRED, UUID.randomUUID())
            viewModel.dreamWithEntries.dreamEntries += newDreamEntry
            updateUI()
        } else {
            viewModel.dreamWithEntries.dream.isFulfilled = false
            viewModel.dreamWithEntries.dream.isDeferred = false
            viewModel.dreamWithEntries.dreamEntries =
                viewModel.dreamWithEntries.dreamEntries.dropLast(1)
            updateUI()
        }
    }

    private fun updateUICheckbox() {
        when {
            viewModel.dreamWithEntries.dream.isDeferred -> {
                binding.dreamDeferredCheckbox.isChecked = true
                binding.dreamFulfilledCheckbox.isChecked = false
                binding.dreamDeferredCheckbox.isEnabled = true
                binding.dreamFulfilledCheckbox.isEnabled = false
            }
            viewModel.dreamWithEntries.dream.isFulfilled -> {
                binding.dreamDeferredCheckbox.isChecked = false
                binding.dreamFulfilledCheckbox.isChecked = true
                binding.dreamDeferredCheckbox.isEnabled = false
                binding.dreamFulfilledCheckbox.isEnabled = true
            }
            else -> {
                binding.dreamDeferredCheckbox.isEnabled = true
                binding.dreamFulfilledCheckbox.isEnabled = true
                binding.dreamDeferredCheckbox.isChecked = false
                binding.dreamFulfilledCheckbox.isChecked = false
            }
        }
    }

    private fun updateUIButtons() {
        var dreamEntryList = viewModel.dreamWithEntries.dreamEntries
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
            } else if (dreamEntry.kind == DreamEntryKind.FULFILLED || viewModel.dreamWithEntries.dream.isFulfilled) {
                button.text = "Fulfilled"
                button.visibility = View.VISIBLE
                val buttonColor = Color.parseColor("#4CAF50")
                button.backgroundTintList = ColorStateList.valueOf(buttonColor)
            } else if (dreamEntry.kind == DreamEntryKind.DEFERRED || viewModel.dreamWithEntries.dream.isDeferred) {
                button.text = "Deferred"
                button.visibility = View.VISIBLE
                val buttonColor = Color.parseColor("#FFBB86FC")
                button.backgroundTintList = ColorStateList.valueOf(buttonColor)
            }
        }
    }


    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int, before: Int, count: Int
            ) {
                viewModel.dream.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {}
        }
        binding.dreamTitleText.addTextChangedListener(titleWatcher)
        binding.dreamFulfilledCheckbox.setOnClickListener {
            onFulfilledClick()
        }

        binding.dreamDeferredCheckbox.setOnClickListener {
            onDeferredClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
