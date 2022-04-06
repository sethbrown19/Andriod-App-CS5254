package edu.vt.cs.cs5254.dreamcatcher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings.System.DATE_FORMAT
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding
import edu.vt.cs.cs5254.dreamcatcher.databinding.ListItemDreamEntryBinding
import java.io.File
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
    private lateinit var adapter: DreamEntryAdapter
    private lateinit var dream: Dream
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var photoLauncher: ActivityResultLauncher<Uri>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dreamWithEntries = DreamWithEntries(Dream(), listOf())
        val dreamId: UUID = arguments?.getSerializable(ARG_DREAM_ID) as UUID
        Log.d(TAG, "Dream detail fragment for dream with ID ${dreamWithEntries.dream.id}")
        viewModel.loadDream(dreamId)
        setHasOptionsMenu(true)
        photoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                updatePhotoView()
            }
            requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.dreamLiveData.observe(
            viewLifecycleOwner,
            Observer { dreamWithEntries ->
                dreamWithEntries?.let {
                    this.dreamWithEntries = dreamWithEntries
                    photoFile = viewModel.getPhotoFile(dreamWithEntries)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "edu.vt.cs.cs5254.dreamcatcher.fileprovider",

                        photoFile)
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
        binding.dreamEntryRecyclerView.layoutManager = LinearLayoutManager(context)
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
        R.id.share_dream.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getDreamReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.dream_report_subject))
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }

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

    // options menu function
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_dream_detail, menu)
        val cameraAvailable = PictureUtils.isCameraAvailable(requireActivity())
        val menuItem = menu.findItem(R.id.take_dream_photo)
        menuItem.apply {
            Log.d(TAG, "Camera available: $cameraAvailable")
            isEnabled = cameraAvailable
            isVisible = cameraAvailable
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.take_dream_photo -> {
                val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                }
                requireActivity().packageManager
                    .queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
                    .forEach { cameraActivity ->
                        requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }
                photoLauncher.launch(photoUri)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
        R.id.share_dream -> {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getDreamReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.dream_report_subject)
                )
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }
    }


    private fun updateUI() {
        binding.dreamTitleText.setText(dreamWithEntries.dream.title)
        adapter = DreamEntryAdapter()
        binding.dreamEntryRecyclerView.adapter = adapter
        updateUICheckbox()
        updatePhotoView()
    }
    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = PictureUtils.getScaledBitmap(photoFile.path, 120, 120)
            binding.dreamPhoto.setImageBitmap(bitmap)
        } else {
            binding.dreamPhoto.setImageDrawable(null)
        }
    }

    private fun getDreamReport(): String {
        val fulfilledString = if (dreamWithEntries.dream.isFulfilled) {
            getString(R.string.dream_report_fulfilled)
        } else {
            getString(R.string.dream_report_deferred)
        }
        val df = DateFormat.getMediumDateFormat(context)
        val dateString = df.format(dreamWithEntries.dream.date)
//        val dateString = DateFormat.format(DATE_FORMAT, dreamWithEntries.dream.date).toString()
        val suspect = getString(R.string.crime_report_no_suspect)
        return getString(R.string.dream_report,
            dreamWithEntries.dream.title, dateString, fulfilledString, suspect)
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
            dreamWithEntries.dreamEntries =
                dreamWithEntries.dreamEntries.filter { dreamEntry -> dreamEntry.kind != DreamEntryKind.FULFILLED }
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
            dreamWithEntries.dreamEntries =
                dreamWithEntries.dreamEntries.filter { dreamEntry -> dreamEntry.kind != DreamEntryKind.DEFERRED }
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

    inner class DreamEntryHolder(private val itemBinding: ListItemDreamEntryBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        private lateinit var dreamWithEntries: DreamWithEntries



        fun bind(dreamEntry: DreamEntry) {
            if (dreamEntry.kind == DreamEntryKind.CONCEIVED) {
                itemBinding.dreamEntryButton.text = "Conceived"
                itemBinding.dreamEntryButton.isEnabled = false
                val buttonColor = Color.parseColor("#0B0FDF")
                itemBinding.dreamEntryButton.backgroundTintList =
                    ColorStateList.valueOf(buttonColor)
            } else if (dreamEntry.kind == DreamEntryKind.REFLECTION) {
                val df = DateFormat.getMediumDateFormat(context)
                val formattedDate = df.format(dreamEntry.date)
                itemBinding.dreamEntryButton.text = formattedDate . toString () + ": " + dreamEntry.text
                val buttonColor = Color.parseColor("#D61D66")
                itemBinding.dreamEntryButton.backgroundTintList =
                    ColorStateList.valueOf(buttonColor)
            } else if (dreamEntry.kind == DreamEntryKind.FULFILLED) {
                itemBinding.dreamEntryButton.text = "Fulfilled"
                val buttonColor = Color.parseColor("#4CAF50")
                itemBinding.dreamEntryButton.backgroundTintList =
                    ColorStateList.valueOf(buttonColor)
            } else if (dreamEntry.kind == DreamEntryKind.DEFERRED) {
                itemBinding.dreamEntryButton.text = "Deferred"
                val buttonColor = Color.parseColor("#FFBB86FC")
                itemBinding.dreamEntryButton.backgroundTintList =
                    ColorStateList.valueOf(buttonColor)
            }
        }
    }


    private inner class DreamEntryAdapter() :
        RecyclerView.Adapter<DreamEntryHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : DreamEntryHolder {
            val itemBinding = ListItemDreamEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DreamEntryHolder(itemBinding)
        }

        override fun getItemCount() = dreamWithEntries.dreamEntries.size

        override fun onBindViewHolder(holder: DreamEntryHolder, position: Int) {
            val dreamEntry = dreamWithEntries.dreamEntries[position]
            holder.bind(dreamEntry)
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
