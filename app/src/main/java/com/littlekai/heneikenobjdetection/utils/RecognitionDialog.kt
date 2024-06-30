package com.littlekai.heneikenobjdetection.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.littlekai.heneikenobjdetection.R

class RecognitionDialog(private val context: Context) {

    fun show(recognizedObjects: List<String>) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_table, null)
        val tableLayout: TableLayout = dialogView.findViewById(R.id.tableLayout)

        val labels = listOf("face", "human", "drinker", "logo", "beer_bottle", "beer_can", "beer_carton", "PG", "poster", "display_stand")
        val labelMap = mutableMapOf<String, Int>()
        val typeMap = mutableMapOf<String, String>()

        recognizedObjects.forEach { label ->
            val parts = label.split("_")
            val baseLabel = parts[0]
            val type = if (parts.size > 1) parts[1] else ""

            labelMap[baseLabel] = (labelMap[baseLabel] ?: 0) + 1
            if (type.isNotEmpty()) {
                typeMap[baseLabel] = type
            }
        }

        labels.forEach { label ->
            val count = labelMap[label] ?: 0
            if (count > 0) {
                val tableRow = TableRow(context)

                val labelTextView = TextView(context).apply {
                    text = label
                    setPadding(8, 8, 8, 8)
                    gravity = android.view.Gravity.CENTER
                }
                val countTextView = TextView(context).apply {
                    text = count.toString()
                    setPadding(8, 8, 8, 8)
                    gravity = android.view.Gravity.CENTER
                }
                val typeTextView = TextView(context).apply {
                    text = typeMap[label] ?: ""
                    setPadding(8, 8, 8, 8)
                    gravity = android.view.Gravity.CENTER
                }

                tableRow.addView(labelTextView)
                tableRow.addView(countTextView)
                tableRow.addView(typeTextView)
                tableLayout.addView(tableRow)

                // Thêm đường phân cách cho mỗi hàng
                val divider = View(context).apply {
                    layoutParams = TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1
                    ).apply {
                        setMargins(0, 1, 0, 1)
                    }
                    setBackgroundColor(context.getColor(android.R.color.holo_red_dark))
                }
                tableLayout.addView(divider)
            }
        }

        AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

}
