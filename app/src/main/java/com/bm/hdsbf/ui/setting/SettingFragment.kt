package com.bm.hdsbf.ui.setting

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListPopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bm.hdsbf.BuildConfig
import com.bm.hdsbf.data.local.sp.PreferenceClass
import com.bm.hdsbf.databinding.FragmentSettingBottomSheetBinding
import com.bm.hdsbf.ui.schedule.ScheduleViewModel
import com.bm.hdsbf.utils.CalendarUtil.displayName
import com.bm.hdsbf.utils.ViewUtil.setGone
import com.bm.hdsbf.utils.ViewUtil.setVisible
import com.bm.hdsbf.utils.ViewUtil.showLongToast
import com.bm.hdsbf.utils.ViewUtil.showShortToast
import com.bm.hdsbf.utils.adapter.ListPopUpWindowAdapter
import com.bm.hdsbf.utils.notification.NotificationManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kizitonwose.calendar.core.yearMonth
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentSettingBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SettingViewModel>()
    private val scheduleViewModel by activityViewModels<ScheduleViewModel>()
    @Inject lateinit var preferenceClass: PreferenceClass
    @Inject lateinit var notificationManager: NotificationManager
    private val listName = mutableListOf<String>()
    private val listShow by lazy { ShowSetting.entries.map { it.text() } }
    private val notificationLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) notificationManager.activate()
        else {
            binding.scReminder.isChecked = false
            requireContext().showShortToast("Ijin notifikasi ditolak")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBottomSheetBinding.inflate(inflater, null, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initObservers()
        viewModel.getName(LocalDate.now().yearMonth.displayName())

        binding.ivClose.setOnClickListener { dismiss() }

        binding.tvName.apply {
            preferenceClass.getName().let {
                if (it == null) setGone() else {
                    setVisible()
                    text = it
                }
            }
        }
        binding.tvShow.text = preferenceClass.getShow() ?: ShowSetting.ALL.text()
        binding.scReminder.isChecked = preferenceClass.getReminder()

        binding.clName.setOnClickListener {
            val popupWindow = ListPopupWindow(requireContext())
            val adapter = ListPopUpWindowAdapter(
                requireContext(), listName, binding.tvName.text.toString(),
                object : ListPopUpWindowAdapter.OnTextClickListener {
                override fun onClick(text: String) {
                    popupWindow.dismiss()
                    binding.tvName.text = text
                    binding.tvName.setVisible()
                    preferenceClass.setName(text)
                    scheduleViewModel.getDataCount()
                }
            })
            popupWindow.setAdapter(adapter)
            popupWindow.anchorView = binding.clName
            popupWindow.show()
        }

        binding.clShow.setOnClickListener {
            val popupWindow = ListPopupWindow(requireContext())
            val adapter = ListPopUpWindowAdapter(
                requireContext(), listShow, binding.tvShow.text.toString(),
                object : ListPopUpWindowAdapter.OnTextClickListener {
                    override fun onClick(text: String) {
                        popupWindow.dismiss()
                        if (text == ShowSetting.NAME.text() && !binding.tvName.isVisible) {
                            requireContext().showLongToast("Pilih nama Anda terlebih dahulu")
                        } else {
                            binding.tvShow.text = text
                            binding.tvShow.setVisible()
                            preferenceClass.setShow(text)
                            scheduleViewModel.getDataCount()
                        }
                    }
                })
            popupWindow.setAdapter(adapter)
            popupWindow.anchorView = binding.clShow
            popupWindow.show()
        }

        binding.scReminder.setOnClickListener {
            val isChecked = binding.scReminder.isChecked
            if (isChecked) {
                if (!binding.tvName.isVisible) {
                    binding.scReminder.isChecked = false
                    requireContext().showLongToast("Pilih nama Anda terlebih dahulu")
                } else {
                    if (SDK_INT >= TIRAMISU) {
                        if (requireActivity().checkSelfPermission(POST_NOTIFICATIONS) == PERMISSION_GRANTED) {
                            notificationManager.activate()
                        } else notificationLauncher.launch(POST_NOTIFICATIONS)
                    } else notificationManager.activate()
                }
            } else notificationManager.cancel()
        }
        binding.scReminder.setOnCheckedChangeListener { _, b -> preferenceClass.setReminder(b) }
        binding.tvScReminder.setOnClickListener { binding.scReminder.performClick() }

        val timeHelpdesk = preferenceClass.getTimeSchedule()
        binding.tvTimeHelpdesk.text =
            timeHelpdesk?.keys?.sorted()?.joinToString(separator = "\n") { "$it : ${timeHelpdesk[it]}" }

        binding.tvVersi.text = BuildConfig.VERSION_NAME
    }

    private fun initObservers() {
        viewModel.listName.observe(viewLifecycleOwner) {
            listName.clear()
            listName.addAll(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}