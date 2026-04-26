package com.bm.hdsbf.data.remote.service

import android.content.Context
import com.bm.hdsbf.R
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
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

    fun getDriveService(): Drive {
        val inputStream = context.resources.openRawResource(R.raw.service_account_key)
        val credential = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf(DriveScopes.DRIVE_METADATA_READONLY))
        return Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            jsonFactory,
            HttpCredentialsAdapter(credential)
        ).setApplicationName("HDSBF-App").build()
    }

    fun getSheetsService(): Sheets {
        val inputStream = context.resources.openRawResource(R.raw.service_account_key)
        val credentials = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf(SheetsScopes.SPREADSHEETS_READONLY))

        return Sheets.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credentials)
        ).setApplicationName("HDSBF-App").build()
    }
}