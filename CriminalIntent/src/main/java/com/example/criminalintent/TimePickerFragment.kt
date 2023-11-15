package com.example.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.util.Date
import java.util.GregorianCalendar
import kotlin.math.min

private const val ARG_DATE = "date"

class TimePickerFragment : DialogFragment() {
    val timeListener =
        TimePickerDialog.OnTimeSetListener { _: TimePicker?, hour: Int, minute: Int ->
            val resultTime: Date = GregorianCalendar(year, month, dayOfMonth, hour, minute).time
            setFragmentResult(KEY_REQUEST, bundleOf(KEY_TIME to resultTime))
        }
    private  var year: Int = 0
    private var month: Int = 0
    private var dayOfMonth: Int = 0
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.getSerializable<Date>(ARG_DATE)

        val calendar = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Calendar.getInstance()
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        calendar.time =date
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val initialHour = calendar.get(Calendar.HOUR)
        val initialMinutes = calendar.get(Calendar.MINUTE)
        return TimePickerDialog(
            requireContext(),
            timeListener,
            initialHour,
            initialMinutes,
            true
        )
    }

    companion object {
        fun newInstance(date: Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }

        const val KEY_REQUEST = "TimePickerFragment"
        const val KEY_TIME = "time"
    }
}