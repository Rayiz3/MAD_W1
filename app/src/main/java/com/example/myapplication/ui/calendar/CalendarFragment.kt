package com.example.myapplication.ui.calendar

import Date
import SharedViewModel
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentCalendarBinding
import com.example.myapplication.ui.contact.CalendarAdapter
import com.example.myapplication.ui.contact.CalendarSingleDay
import com.example.myapplication.ui.gallery.GalleryAdapter
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textTitleView: TextView = binding.calendarTitle
        val textSubtitleView: TextView = binding.calendarSubtitle

        // Set the title
        textTitleView.text = getString(R.string.title_calendar)
        textSubtitleView.text = getString(R.string.subtitle_calendar)

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startDateButton: Button = view.findViewById(R.id.startDate)
        val endDateButton: Button = view.findViewById(R.id.endDate)
        val calendar = Calendar.getInstance()

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView_calendar)

        // Set the RecyclerView's layout manager
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Start Date Picker
        startDateButton.setOnClickListener {
            val startDatePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    sharedViewModel.setStartDate(Date(year, month, dayOfMonth))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            startDatePicker.show()
        }

        // End Date Picker
        endDateButton.setOnClickListener {
            val endDatePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    sharedViewModel.setEndDate(Date(year, month, dayOfMonth))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            endDatePicker.show()
        }

        // observing dates
        sharedViewModel.startDate.observe(viewLifecycleOwner) { date ->
            Log.v("start date", date.toString())
            startDateButton.text = getString(R.string.date_calendar, date.year%100, date.month + 1, date.day)
        }

        sharedViewModel.endDate.observe(viewLifecycleOwner) { date ->
            Log.v("end date", date.toString())
            endDateButton.text = getString(R.string.date_calendar, date.year%100, date.month + 1, date.day)
        }

        // observing dates difference
        sharedViewModel.dateDifference.observe(viewLifecycleOwner) { difference ->
            val cardViewDescription = view.findViewById<CardView>(R.id.calendar_description)
            if (difference != null) {
                val startDate = sharedViewModel.startDate.value
                val endDate = sharedViewModel.endDate.value
                if (startDate != null && endDate != null) {
                    val dayList = if (difference >= 0) (0..difference.toInt()).map { offset ->
                        LocalDate.of(startDate.year, startDate.month + 1, startDate.day)
                            .plusDays(offset.toLong())
                            .dayOfMonth
                    } else listOf()

                    val weekdayList = if (difference >= 0) (0..difference.toInt()).map { offset ->
                        LocalDate.of(startDate.year, startDate.month + 1, startDate.day)
                            .plusDays(offset.toLong())
                            .dayOfWeek
                            .getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                    } else listOf()

                    val combinedList = dayList.zip(weekdayList).map { (day, weekday) -> CalendarSingleDay(day, weekday) }
                    calendarAdapter = CalendarAdapter(combinedList)
                    recyclerView.adapter = calendarAdapter

                    cardViewDescription.visibility = View.VISIBLE
                } else {
                    cardViewDescription.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}