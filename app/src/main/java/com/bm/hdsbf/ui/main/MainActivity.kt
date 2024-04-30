package com.bm.hdsbf.ui.main

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bm.hdsbf.R
import com.bm.hdsbf.databinding.ActivityMainBinding
import com.bm.hdsbf.ui.schedule.ScheduleActivity
import com.bm.hdsbf.ui.update.DownloadFragment
import com.bm.hdsbf.ui.update.InstallFragment
import com.bm.hdsbf.ui.update.UpdateFragment
import com.bm.hdsbf.utils.ViewUtil.setGone
import com.bm.hdsbf.utils.ViewUtil.setVisible
import com.bm.hdsbf.utils.ViewUtil.showShortToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()
    private val updateDialog by lazy { UpdateFragment() }
    private val downloadDialog by lazy { DownloadFragment() }
    private val installDialog by lazy { InstallFragment() }
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) download()
        else showShortToast("Ijin penyimpanan ditolak")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getLastVersionApp()
    }

    @SuppressLint("SetTextI18n")
    private fun initObservers() {
        viewModel.loadingSchedule.observe(this) {
            binding.tvProgress.text = "Mengambil Data"
            binding.llcContainerLoading.apply { if (it) setVisible() else setGone() }
        }
        viewModel.error.observe(this) {
            showShortToast(it)
        }
        viewModel.progress.observe(this) {
            binding.tvProgress.text = "Mengambil Data ($it%)"
            if (it == 100) {
                startActivity(Intent(this, ScheduleActivity::class.java))
                finishAfterTransition()
            }
        }

        viewModel.loadingUpdate.observe(this) {
            binding.tvProgress.text = "Checking Update"
            binding.llcContainerLoading.apply { if (it) setVisible() else setGone() }
        }
        viewModel.forceUpdate.observe(this) {
            when (it) {
                null -> viewModel.getAllData()
                else -> requestUpdate(it)
            }
        }
    }

    private fun requestUpdate(forceUpdate: Boolean) {
        updateDialog.forceUpdate = forceUpdate
        updateDialog.isCancelable = false
        updateDialog.onContinue = { viewModel.getAllData() }
        updateDialog.onDownload = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                permissionLauncher.launch(WRITE_EXTERNAL_STORAGE)
            } else { download() }
        }
        updateDialog.show(supportFragmentManager, null)
    }

    private fun download() {
        downloadDialog.isCancelable = false
        downloadDialog.onError = {
            showShortToast(it)
            viewModel.getAllData()
        }
        downloadDialog.onSuccess = {
            installDialog.isCancelable = false
            installDialog.fileName = it
            installDialog.show(supportFragmentManager, null)
        }
        downloadDialog.show(supportFragmentManager, null)
    }
}