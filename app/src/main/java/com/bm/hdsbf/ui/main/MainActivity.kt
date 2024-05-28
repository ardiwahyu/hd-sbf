package com.bm.hdsbf.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bm.hdsbf.data.local.sp.PreferenceClass
import com.bm.hdsbf.data.remote.config.RemoteConfig
import com.bm.hdsbf.databinding.ActivityMainBinding
import com.bm.hdsbf.ui.schedule.ScheduleActivity
import com.bm.hdsbf.ui.update.UpdateFragment
import com.bm.hdsbf.utils.ViewUtil.dialogError
import com.bm.hdsbf.utils.ViewUtil.setGone
import com.bm.hdsbf.utils.ViewUtil.setVisible
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()
    private val updateDialog by lazy { UpdateFragment() }
    @Inject lateinit var remoteConfig: RemoteConfig
    @Inject lateinit var preferenceClass: PreferenceClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
            viewModel.getLastVersionApp()
        }
        firebaseRemoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                firebaseRemoteConfig.activate()
            }
            override fun onError(error: FirebaseRemoteConfigException) { }
        })

        initObservers()
    }

    @SuppressLint("SetTextI18n")
    private fun initObservers() {
        viewModel.loadingSchedule.observe(this) {
            binding.tvProgress.text = "Mengambil Data"
            binding.llcContainerLoading.apply { if (it) setVisible() else setGone() }
        }
        viewModel.error.observe(this) {
            if (preferenceClass.getIsFirst()) {
                val dialog = dialogError(
                    "Perhatian",
                    "Gagal mengambil data, cek koneksi internet Anda",
                    "Muat Ulang", false
                ) {
                    viewModel.getLastVersionApp()
                    it.dismiss()
                }
                dialog.show()
            } else {
                startActivity(Intent(this, ScheduleActivity::class.java))
                finishAfterTransition()
            }
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
            val url = "https://drive.google.com/file/d/${remoteConfig.getAppId()}/view?usp=sharing"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        updateDialog.show(supportFragmentManager, null)
    }
}