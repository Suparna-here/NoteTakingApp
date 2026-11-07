package com.example.compose.notetakingapp.presentation.notes

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.compose.notetakingapp.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NoteScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun addNote_displaysInList() {
        val testTitle = "My Test Title"
        val testContent = "My Test Content"

        // Type in title
        composeRule.onNodeWithText("Title").performTextInput(testTitle)
        
        // Type in content
        composeRule.onNodeWithText("Content").performTextInput(testContent)
        
        // Click Add Note button
        composeRule.onNodeWithText("Add Note").performClick()

        // Verify the note is displayed on the screen
        composeRule.onNodeWithText(testTitle).assertIsDisplayed()
        composeRule.onNodeWithText(testContent).assertIsDisplayed()
    }

    @Test
    fun addEmptyNote_showsSnackbar() {
        // Click Add Note button without entering text
        composeRule.onNodeWithText("Add Note").performClick()

        // Verify the snackbar message is displayed (Title can't be empty)
        composeRule.onNodeWithText("The title of the note can't be empty.").assertIsDisplayed()
    }
}
