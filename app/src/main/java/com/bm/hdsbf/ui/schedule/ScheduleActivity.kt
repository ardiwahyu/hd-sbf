package com.bm.hdsbf.ui.schedule

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import com.bm.hdsbf.R
import com.bm.hdsbf.databinding.ActivityScheduleBinding
import com.bm.hdsbf.databinding.CalendarDayLayoutBinding
import com.bm.hdsbf.ui.setting.SettingFragment
import com.bm.hdsbf.utils.CalendarUtil.daysOfWeek
import com.bm.hdsbf.utils.CalendarUtil.displayName
import com.bm.hdsbf.utils.ViewUtil.setGone
import com.bm.hdsbf.utils.ViewUtil.setVisible
import com.bm.hdsbf.utils.ViewUtil.showShortToast
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleBinding
    private val viewModel: ScheduleViewModel by viewModels()
    @Inject lateinit var scheduleAdapter: ScheduleAdapter
    private val today by lazy { LocalDate.now() }
    private var selectedDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.rvSchedule.adapter = scheduleAdapter

        binding.ivBefore.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.previousMonth)
            }
        }
        binding.ivNext.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.nextMonth)
            }
        }
        binding.ivSetting.setOnClickListener {
            val dialog = SettingFragment()
            dialog.show(supportFragmentManager, null)
        }

        initObservers()
        viewModel.getDataCount()

        if (savedInstanceState == null) {
            binding.calendarView.post { selectDate(today) }
        }
    }

    private fun initObservers() {
        viewModel.dataCount.observe(this) {
            initView(it)
        }
        viewModel.loading.observe(this) {

        }
        viewModel.error.observe(this) {
            showShortToast(it)
        }
        viewModel.listSchedule.observe(this) {
            scheduleAdapter.submitList(it)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            try {
                binding.tvDateSelected.text = "${date.dayOfMonth} ${date.yearMonth.displayName()}"
                viewModel.getScheduleByDate(date.yearMonth.displayName(), date.dayOfMonth)

                val oldDate = selectedDate
                selectedDate = date
                oldDate?.let { binding.calendarView.notifyDateChanged(it) }
                binding.calendarView.notifyDateChanged(date)
            } catch (_: Exception) {}
        }
    }

    private fun initView(dataCount: HashMap<String, HashMap<Int, List<String>>>) {
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.date = data
                container.dayLayoutBinding.calendarDayText.text = data.date.dayOfMonth.toString()
                dataCount[data.date.yearMonth.displayName()]?.get(data.date.dayOfMonth).let { listHd ->
                    container.dayLayoutBinding.viewHd1.apply { if (listHd?.contains("HD1") == true) setVisible() else setGone()}
                    container.dayLayoutBinding.viewHd2.apply { if (listHd?.contains("HD2") == true) setVisible() else setGone()}
                    container.dayLayoutBinding.viewHd3.apply { if (listHd?.contains("HD3") == true) setVisible() else setGone()}
                }
                if (data.position == DayPosition.MonthDate) {
                    container.dayLayoutBinding.calendarDayText.textSize = 16f
                    when (data.date) {
                        today -> {
                            container.dayLayoutBinding.calendarDayText.setTextColor(getColor(R.color.colorPrimary))
                            container.dayLayoutBinding.calendarDayText.typeface = resources.getFont(R.font.nunito_bold)
                        }
                        selectedDate -> {
                            container.dayLayoutBinding.calendarDayText.setTextColor(getColor(R.color.black))
                            container.dayLayoutBinding.calendarDayText.typeface = resources.getFont(R.font.nunito_bold)
                        }
                        else -> {
                            container.dayLayoutBinding.calendarDayText.setTextColor(getColor(R.color.black))
                            container.dayLayoutBinding.calendarDayText.typeface = resources.getFont(R.font.nunito_regular)
                        }
                    }
                } else {
                    container.dayLayoutBinding.calendarDayText.setTextColor(Color.GRAY)
                    container.dayLayoutBinding.calendarDayText.textSize = 12f
                }
            }

            override fun create(view: View) = DayViewContainer(view)
        }
        binding.calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                if (container.titlesContainer.tag == null) {
                    container.titlesContainer.tag = data.yearMonth
                    container.titlesContainer.children.map { it as TextView }
                        .forEachIndexed { index, textView ->
                            val dayOfWeek = daysOfWeek[index]
                            textView.text = dayOfWeek
                        }
                }
            }

            override fun create(view: View) = MonthViewContainer(view)
        }
        binding.calendarView.monthScrollListener = {
            binding.tvMonth.text = it.yearMonth.displayName()
        }
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)
        val firstDayOfWeek = DayOfWeek.MONDAY
        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)
    }

    inner class DayViewContainer(view: View): ViewContainer(view) {
        lateinit var date: CalendarDay
        val dayLayoutBinding = CalendarDayLayoutBinding.bind(view)
        init { view.setOnClickListener { selectDate(date.date) } }
    }

    class MonthViewContainer(view: View) : ViewContainer(view) {
        val titlesContainer = view as ViewGroup
    }
}