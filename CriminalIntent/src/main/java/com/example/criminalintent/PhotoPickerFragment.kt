package com.example.criminalintent

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment

private const val KEY_PATH = " path"

class PhotoPickerFragment : DialogFragment() {
    private lateinit var imageView: ImageView
    private lateinit var path: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        path = arguments?.getString(KEY_PATH) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.pick_image, container, false)
        imageView = view.findViewById(R.id.crime_image)

        val bitmap = getScaledBitmap(path,requireActivity())
        imageView.setImageBitmap(bitmap)

        return view
    }

    companion object {
        fun newInstance(path: String): PhotoPickerFragment {
            val photoPickerFragment = PhotoPickerFragment()
            val args = Bundle()
            args.putString(KEY_PATH,path)
            photoPickerFragment.arguments = args
            return photoPickerFragment
        }
    }
}