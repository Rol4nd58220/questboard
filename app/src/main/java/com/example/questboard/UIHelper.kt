package com.example.questboard

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView

object UIHelper {

    /**
     * Sets "QuestBoard" text with orange "Q" and white "uestBoard"
     */
    fun setQuestBoardTitle(textView: TextView) {
        val text = "QuestBoard"
        val spannableString = SpannableString(text)

        // Orange color for "Q"
        val orangeColor = Color.parseColor("#FF9800")
        spannableString.setSpan(
            ForegroundColorSpan(orangeColor),
            0, 1, // Start and end position for "Q"
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannableString
    }
}

