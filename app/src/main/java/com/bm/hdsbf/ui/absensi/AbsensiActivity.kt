package com.bm.hdsbf.ui.absensi

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bm.hdsbf.R
import com.bm.hdsbf.data.local.sp.PreferenceClass
import com.bm.hdsbf.data.remote.model.LokasiAbsensi
import com.bm.hdsbf.databinding.ActivityAbsensiBinding
import com.bm.hdsbf.utils.ViewUtil
import com.bm.hdsbf.utils.ViewUtil.dialogError
import com.bm.hdsbf.utils.ViewUtil.setGone
import com.bm.hdsbf.utils.ViewUtil.setInvisible
import com.bm.hdsbf.utils.ViewUtil.setVisible
import com.bm.hdsbf.utils.ViewUtil.showLongToast
import com.bm.hdsbf.utils.ViewUtil.showShortToast
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@AndroidEntryPoint
class AbsensiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAbsensiBinding
    private val viewModel by viewModels<AbsensiViewModel>()
    private val dialogLoading by lazy { ViewUtil.dialogLoading(this) }

    @Inject lateinit var preferenceClass: PreferenceClass
    private var location: LokasiAbsensi.Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAbsensiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        iniObservers()
    }

    private fun initView() {
        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnPeriksa.setOnClickListener {
            if (binding.etNik.text.toString().trim().isEmpty() ||
                binding.etPass.text.toString().trim().isEmpty() ||
                binding.etIdDevice.text.toString().trim().isEmpty()) {
                showShortToast("Isi sek cuyy..")
            } else {
                viewModel.login(
                    binding.etNik.text.toString(),
                    binding.etPass.text.toString(),
                    binding.etIdDevice.text.toString()
                )
            }
        }
        binding.btnMasuk.setOnClickListener {
            if (location != null) {
                val posisi = getRandomLatLonInCircle(location!!.latitude, location!!.longitude, location!!.radius)
                viewModel.absen(location!!.id, posisi.first, posisi.second, "start")
            } else {
                showLongToast("lokasi null, mencoba get config baru...")
                viewModel.getConfig()
            }
        }
        binding.btnPulang.setOnClickListener {
            if (location != null) {
                val posisi = getRandomLatLonInCircle(location!!.latitude, location!!.longitude, location!!.radius)
                viewModel.absen(location!!.id, posisi.first, posisi.second, "end")
            } else {
                showLongToast("lokasi null, mencoba get config baru...")
                viewModel.getConfig()
            }
        }

        writeUser()
        if (!preferenceClass.getString("user").isNullOrEmpty()) {
            viewModel.getConfig()
        } else {
            binding.llcContainerButton.setGone()
        }
    }

    private fun iniObservers() {
        viewModel.loading.observe(this) {
            if (it) dialogLoading.show() else dialogLoading.dismiss()
        }
        viewModel.error.observe(this) {
            showShortToast(it)
        }
        viewModel.configResult.observe(this) {
            location = it.location.maxBy { l -> l.radius }
        }
        viewModel.user.observe(this) {
            viewModel.getConfig()
            preferenceClass.setString("user", Gson().toJson(it))
            binding.llcContainerButton.setVisible()
            writeUser()
        }
        viewModel.absensiResult.observe(this) {
            val dialog = dialogError("Perhatian", it, "Oke", true) { d -> d.dismiss() }
            dialog.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun writeUser() {
        val userJson = preferenceClass.getString("user")
        if (userJson != null) {
//            val user = Gson().fromJson(userJson, UserAbsensi::class.java)
//            binding.tvResult.text = "Nama: ${user.employee.name}\nNik: ${user.employee.number}\nPosisi: ${user.employee.position}"
            binding.tvNote.setVisible()
            binding.tvNote.text = "Anda sudah login, login lagi hanya ketika absen error"
            binding.etNik.setText(preferenceClass.getString("username"))
            binding.etPass.setText(preferenceClass.getString("password"))
            binding.etIdDevice.setText(preferenceClass.getIdDevice())
        } else {
            binding.tvNote.setInvisible()
        }
    }

    private fun getRandomLatLonInCircle(centerLat: Double, centerLon: Double, radius: Double): Pair<Double, Double> {
        val earthRadius = 6371000.0
        val randomDistance = radius * sqrt(Math.random())
        val randomAngle = 2 * Math.PI * Math.random()
        val deltaLat = (randomDistance / earthRadius) * cos(randomAngle)
        val deltaLon = (randomDistance / (earthRadius * cos(Math.toRadians(centerLat)))) * sin(randomAngle)
        val newLat = centerLat + Math.toDegrees(deltaLat)
        val newLon = centerLon + Math.toDegrees(deltaLon)
        return Pair(newLat, newLon)
    }
}