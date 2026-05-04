package com.bm.hdsbf.data.remote.service

import android.content.Context
import com.bm.hdsbf.R
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GoogleService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val transport = GoogleNetHttpTransport.newTrustedTransport()

    private val scopes = listOf(
        SheetsScopes.SPREADSHEETS,
        DriveScopes.DRIVE_METADATA_READONLY
    )

    private fun getCredentials(): GoogleCredentials {
        val inputStream = context.resources.openRawResource(R.raw.service_account_key)
        return GoogleCredentials.fromStream(inputStream).createScoped(scopes)
    }

    fun getDriveService(): Drive {
        return Drive.Builder(
            transport,
            jsonFactory,
            HttpCredentialsAdapter(getCredentials())
        ).setApplicationName("HDSBF-App").build()
    }

    fun getSheetsService(): Sheets {
        return Sheets.Builder(
            transport,
            jsonFactory,
            HttpCredentialsAdapter(getCredentials())
        ).setApplicationName("HDSBF-App").build()
    }
}