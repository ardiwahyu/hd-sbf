package com.bm.hdsbf.ui.shiftswap

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.bm.hdsbf.data.local.db.entities.ScheduleVo
import com.bm.hdsbf.databinding.ActivityShiftSwapBinding
import com.bm.hdsbf.ui.BaseActivity
import com.bm.hdsbf.ui.schedule.ScheduleActivity
import com.bm.hdsbf.ui.schedule.ScheduleAdapter
import com.bm.hdsbf.ui.schedule.ScheduleViewModel
import com.bm.hdsbf.utils.CalendarUtil.displayName
import com.bm.hdsbf.utils.CalendarUtil.formatLocalDate
import com.bm.hdsbf.utils.CalendarUtil.showDatePicker
import com.bm.hdsbf.utils.ViewUtil.dialogLoading
import com.bm.hdsbf.utils.ViewUtil.setGone
import com.bm.hdsbf.utils.ViewUtil.setVisible
import com.bm.hdsbf.utils.ViewUtil.showLongToast
import com.bm.hdsbf.utils.ViewUtil.showShortToast
import com.bm.hdsbf.utils.viewBinding
import com.kizitonwose.calendar.core.yearMonth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShiftSwapActivity : BaseActivity() {
    private val binding by viewBinding(ActivityShiftSwapBinding::inflate)
    private val scheduleViewModel by viewModels<ScheduleViewModel>()
    private val viewModel by viewModels<ShitSwapViewModel>()

    @Inject lateinit var myScheduleAdapter: ScheduleAdapter
    @Inject lateinit var yourScheduleAdapter: ScheduleAdapter

    private val dialogLoading by lazy { dialogLoading(this) }

    private var selectActive = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.rvMySchedule.adapter = myScheduleAdapter
        binding.etMyTgl.setOnClickListener {
            selectActive = "my"
            showDatePicker {
                binding.etMyTgl.text = it.formatLocalDate()
                scheduleViewModel.getScheduleByDate(it.yearMonth.displayName(), it.dayOfMonth)
            }
        }

        yourScheduleAdapter.enableClick = true
        yourScheduleAdapter.onClick = {
            processYourSchedule(yourScheduleAdapter.currentList, it)
        }
        binding.rvYourSchedule.adapter = yourScheduleAdapter
        binding.etYourTgl.setOnClickListener {
            selectActive = "your"
            showDatePicker {
                binding.etYourTgl.text = it.formatLocalDate()
                scheduleViewModel.getScheduleByDate(it.yearMonth.displayName(), it.dayOfMonth)
            }
        }

        binding.btnTukar.setOnClickListener {
            val mySelected = myScheduleAdapter.currentList.firstOrNull()
            val yourSelectedList = yourScheduleAdapter.currentList
            val yourSelected = yourSelectedList.firstOrNull()
            if (mySelected == null) {
                showShortToast("Pilih jadwalmu terlebih dahulu")
                return@setOnClickListener
            }
            when {
                yourSelectedList.isEmpty() -> {
                    showShortToast("Pilih jadwal lawan yang ingin ditukar")
                }
                yourSelectedList.size > 1 -> {
                    showShortToast("Hanya boleh memilih satu jadwal untuk ditukar")
                }
                else -> {
                    viewModel.swapSchedule(mySelected, yourSelected!!)
                    binding.btnTukar.isEnabled = false
                }
            }
        }

        initObservers()
    }

    private fun initObservers() {
        scheduleViewModel.error.observe(this) {
            showShortToast(it)
        }
        scheduleViewModel.listSchedule.observe(this) {
            if (selectActive == "my") {
                processMySchedule(it)
            } else processYourSchedule(it)
        }
        viewModel.loading.observe(this) {
            if (it) dialogLoading.show() else {
                binding.btnTukar.isEnabled = true
                dialogLoading.dismiss()
            }
        }
        viewModel.error.observe(this) {
            showLongToast(it)
        }
        viewModel.updateResult.observe(this) {
            if (it) {
                showLongToast("Berhasil mengubah data")
                startActivity(Intent(this, ScheduleActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
            }
        }
    }

    private fun processMySchedule(list: List<ScheduleVo>) {
        val myName = preferenceClass.getName()
        val listFilter = list.filter { it.name == myName }
        if (listFilter.isEmpty()) {
            binding.rvMySchedule.setGone()
            showShortToast("Tidak ada jadwalmu di tanggal ini")
            binding.etMyTgl.text = ""
        } else {
            binding.rvMySchedule.setVisible()
            myScheduleAdapter.submitList(listFilter)
        }
    }

    private fun processYourSchedule(list: List<ScheduleVo>, selected: ScheduleVo? = null) {
        val myName = preferenceClass.getName()
        val list1 = list.filter { it.name != myName }
        if (list1.isEmpty()) {
            showShortToast("Tidak ada jadwal yang bisa ditukar")
            binding.etYourTgl.text = ""
            binding.llcScheduleAvailable.setGone()
            return
        }
        if (selected != null) {
            val filteredList = list1.filter { it == selected }
            yourScheduleAdapter.submitList(filteredList)
        } else {
            yourScheduleAdapter.submitList(list1)
        }
        binding.llcScheduleAvailable.setVisible()
    }
}