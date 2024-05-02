package com.bm.hdsbf.data.repository.google

import com.bm.hdsbf.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


class GoogleSheetScraper @Inject constructor() {
    companion object {
        private const val URL = "https://docs.google.com/spreadsheets/d/${BuildConfig.SPREADSHEET_ID}/edit"
    }
    private val df = SimpleDateFormat("MMMM yyyy", Locale("ID"))

    suspend fun getMonthAvailable(): Flow<List<String>> {
        val list = mutableListOf<String>()
        return flow {
            try {
                val doc = Jsoup.connect(URL).get()
                val tabs = doc.getElementsByClass("docs-sheet-tab")
                for (tab in tabs) {
                    val tabName = tab.getElementsByClass("docs-sheet-tab-caption").first()
                    try {
                        df.parse(tabName.text())
                        list.add(tabName.text())
                    } catch (e: Exception) {
                        continue
                    }
                }
                emit(list)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(list)
            }
        }.catch {
            it.printStackTrace()
            emit(list)
        }.flowOn(Dispatchers.IO)
    }
}