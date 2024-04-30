package com.bm.hdsbf.ui.update

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bm.hdsbf.databinding.FragmentDownloadBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DownloadFragment: BottomSheetDialogFragment() {
    private var _binding: FragmentDownloadBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<UpdateViewModel>()

    var onSuccess: ((fileName: String) -> Unit)? = null
    var onError: ((error: String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadBinding.inflate(inflater, null, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initObservers()
        viewModel.download()
    }

    @SuppressLint("SetTextI18n")
    private fun initObservers() {
        viewModel.downloading.observe(viewLifecycleOwner) {
            binding.pbProgress.setProgress(it)
            binding.tvStatus.text = "Downloading Update ($it)%"
        }
        viewModel.downloadFinish.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(1000)
                dismiss()
                onSuccess?.invoke(it)
            }
        }
        viewModel.error.observe(viewLifecycleOwner) {
            dismiss()
            onError?.invoke(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}