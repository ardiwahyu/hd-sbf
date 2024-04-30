package com.bm.hdsbf.ui.update

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bm.hdsbf.databinding.FragmentInstallBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InstallFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentInstallBinding? = null
    private val binding get() = _binding!!

    var fileName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstallBinding.inflate(inflater, null, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvFileLoc.text = "File tersimpan di:\n$fileName"
//        binding.tvFileLoc.setOnClickListener {
//            val file = File(fileName!!)
//            val uri = Uri.fromFile(file)
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            intent.setDataAndType(uri, "application/vnd.android.package-archive")
//            requireContext().startActivity(intent)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}