package com.bm.hdsbf.ui.update

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bm.hdsbf.databinding.FragmentUpdateBinding
import com.bm.hdsbf.utils.ViewUtil.setVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    var forceUpdate: Boolean = false
    var onContinue: (() -> Unit)? = null
    var onDownload: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateBinding.inflate(inflater, null, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (forceUpdate) binding.llcContainerForce.setVisible()
        else binding.llcContainerNotForce.setVisible()

        binding.btnDownload1.setOnClickListener { dismiss(); onDownload?.invoke() }
        binding.btnDownload2.setOnClickListener { dismiss(); onDownload?.invoke() }
        binding.btnNanti.setOnClickListener { dismiss(); onContinue?.invoke() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}