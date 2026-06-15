package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("sympto", appName)
  }

  @Test
  fun `test parseGeminiResponse tag based extraction`() {
    val rawText = """
      Hello there, here are some safety guides for you.
      [DURATION-LADDER]
      Do not try cold medication for more than 4 days. If cough persists, seek qualified medical care.
      [END-DURATION-LADDER]
      Please consult your GP for other medications.
    """.trimIndent()

    val parsed = com.example.ui.parseGeminiResponse(rawText)
    assertEquals(
      "Do not try cold medication for more than 4 days. If cough persists, seek qualified medical care.",
      parsed.durationLadderAdvice
    )
    assertEquals(
      "Hello there, here are some safety guides for you.\n\nPlease consult your GP for other medications.",
      parsed.mainExplanation
    )
  }

  @Test
  fun `test parseGeminiResponse fallback marker extraction`() {
    val rawText = """
      Primary therapeutic details.
      Duration-Ladder: Try this treatment for up to 3 days only.
    """.trimIndent()

    val parsed = com.example.ui.parseGeminiResponse(rawText)
    assertEquals(
      "Try this treatment for up to 3 days only.",
      parsed.durationLadderAdvice
    )
    assertEquals(
      "Primary therapeutic details.",
      parsed.mainExplanation
    )
  }

  @Test
  fun `test parseGeminiResponse multi tag pharmacist extraction`() {
    val rawText = """
      Overview of treatment options.
      [SUGGESTED-MEDICATION]
      Paracetamol 500mg tablets every 6 hours.
      [END-SUGGESTED-MEDICATION]
      
      [DURATION-LADDER]
      Do not take this for more than 3 consecutive days.
      [END-DURATION-LADDER]
      
      [POTENTIAL-SIDE-EFFECTS]
      May cause mild drowsiness. Avoid alcohol.
      [END-POTENTIAL-SIDE-EFFECTS]
      
      Consult your local healthcare worker.
    """.trimIndent()

    val parsed = com.example.ui.parseGeminiResponse(rawText)
    assertEquals(
      "Paracetamol 500mg tablets every 6 hours.",
      parsed.suggestedMedication
    )
    assertEquals(
      "Do not take this for more than 3 consecutive days.",
      parsed.durationLadderAdvice
    )
    assertEquals(
      "May cause mild drowsiness. Avoid alcohol.",
      parsed.potentialSideEffects
    )
    assertEquals(
      "Overview of treatment options.\n\nConsult your local healthcare worker.",
      parsed.mainExplanation
    )
  }
}
