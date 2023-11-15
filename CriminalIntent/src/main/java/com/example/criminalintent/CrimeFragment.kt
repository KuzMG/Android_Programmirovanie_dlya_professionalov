package com.example.criminalintent

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import java.io.File
import java.util.Date
import java.util.UUID


private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val DIALOG_PHOTO = "DialogPhoto"
private const val DATE_FORMAT_DATE = "EdMMMyyyy"
private const val DATE_FORMAT_TIME = "HH:mm"

class CrimeFragment : Fragment() {

    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var callButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var photoUri: Uri
    private var heightPhotoView = 0
    private var widthPhotoView = 0

    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels()

    val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                call()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Вы запретили обращение к контактам!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    val captureImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                requireActivity().revokeUriPermission(
                    photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                updatePhotoView()
            }
        }
    val suspectLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val contactUri: Uri? = result.data?.data
                val queryFields = arrayOf(
                    CommonDataKinds.Phone.DISPLAY_NAME,
                    CommonDataKinds.Phone.HAS_PHONE_NUMBER,
                    CommonDataKinds.Phone.NUMBER
                )
                val cursor = contactUri?.let {
                    requireActivity().contentResolver.query(
                        it,
                        queryFields,
                        null,
                        null,
                        null
                    )
                }
                cursor?.use {
                    if (it.count == 0) {
                        return@use
                    }
                    it.moveToFirst()


                    val suspect = it.getString(0)
                    val hasPhoneNumber = Integer.parseInt(it.getString(1))
                    lateinit var number: String
                    if (hasPhoneNumber > 0)
                        number = it.getString(2)

                    crime.suspect = suspect
                    crime.phone = number
                    crimeDetailViewModel.saveCrime(crime)
                }
            }
        }

    companion object {
        fun getBundleCrimeId(crimeId: UUID) = bundleOf(ARG_CRIME_ID to crimeId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID? = arguments?.getSerializable<UUID>(ARG_CRIME_ID)
        crimeId?.let { crimeDetailViewModel.loadCrime(it) }

        setFragmentResultListener(DatePickerFragment.KEY_REQUEST) { requestKey, bundle ->
            val result = bundle.getSerializable<Date>(DatePickerFragment.KEY_DATE)
            crime.date = result ?: Date()
            updateUI()
        }

        setFragmentResultListener(TimePickerFragment.KEY_REQUEST) { requestKey, bundle ->
            val result = bundle.getSerializable<Date>(TimePickerFragment.KEY_TIME)
            crime.date.time = result?.time ?: crime.date.time
            updateUI()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title)
        dateButton = view.findViewById(R.id.crime_date)
        solvedCheckBox = view.findViewById(R.id.crime_solved)
        timeButton = view.findViewById(R.id.crime_time)
        reportButton = view.findViewById(R.id.crime_report)
        suspectButton = view.findViewById(R.id.crime_suspect)
        callButton = view.findViewById(R.id.crime_call)
        photoView = view.findViewById(R.id.crime_photo)
        photoButton = view.findViewById(R.id.crime_camera)

        val observer = photoView.viewTreeObserver
        observer.addOnGlobalLayoutListener {
            heightPhotoView = photoView.height
            widthPhotoView = photoView.width
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(viewLifecycleOwner) { crime ->
            crime?.let {
                this.crime = crime
                photoFile = crimeDetailViewModel.getPhotoFile(crime)
                photoUri = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.example.android.criminalintent.fileprovider",
                    photoFile
                )
                updateUI()
            }
        }
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        val locale = getCurrentLocale(requireContext())
        val dateFormaDate = DateFormat.getBestDateTimePattern(locale, DATE_FORMAT_DATE)
        val dateFormaTime = DateFormat.getBestDateTimePattern(locale, DATE_FORMAT_TIME)
        dateButton.text = DateFormat.format(dateFormaDate, crime.date)
        timeButton.text = DateFormat.format(dateFormaTime, crime.date)
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }
        callButton.isEnabled = crime.phone.isNotEmpty()
        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, widthPhotoView, heightPhotoView)
            photoView.setImageBitmap(bitmap)
            photoView.contentDescription = getString(R.string.crime_photo_image_description)
        } else {
            photoView.setImageDrawable(null)
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val locale = getCurrentLocale(requireContext())
        val dateFormat = DateFormat.getBestDateTimePattern(locale, DATE_FORMAT_DATE)
        val dateString = DateFormat.format(dateFormat, crime.date).toString()

        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect, crime.phone)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isCheacked ->
                crime.isSolved = isCheacked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_TIME)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject)
                ).also { intent ->
                    val chooserIntent =
                        Intent.createChooser(intent, getString(R.string.send_report))
                    startActivity(chooserIntent)
                }
            }
        }

        val pickContactIntent =
            Intent(Intent.ACTION_PICK, CommonDataKinds.Phone.CONTENT_URI)
        val resolvedActivity: ResolveInfo? = requireActivity()
            .packageManager
            .resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resolvedActivity == null) {
            suspectButton.isEnabled = false
            callButton.isEnabled = false
        }

        suspectButton.setOnClickListener {
            suspectLauncher.launch(pickContactIntent)
        }

        callButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    call()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) -> {
                    Toast.makeText(
                        requireContext(),
                        "Вы запретили обращение к контактам!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    requestPermission.launch(Manifest.permission.CALL_PHONE)
                }
            }
        }

        photoButton.apply {
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val packageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities = packageManager.queryIntentActivities(
                    captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                captureImageLauncher.launch(captureImage)
            }
        }

        photoView.setOnClickListener {
            if (photoFile.exists())
                PhotoPickerFragment.newInstance(photoFile.path).apply {
                    show(this@CrimeFragment.parentFragmentManager, DIALOG_PHOTO)
                }
        }
    }

    fun call() {
        val uri = Uri.parse("tel:" + crime.phone)
        val callIntent = Intent(Intent.ACTION_CALL, uri)
        val chosenIntent = Intent.createChooser(callIntent, "")
        startActivity(chosenIntent)
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }
}