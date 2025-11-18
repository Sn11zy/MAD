package com.example.sportsorganizer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sportsorganizer.data.local.dbs.AppDatabase
import com.example.sportsorganizer.data.local.entities.User
import com.example.sportsorganizer.data.local.session.SessionManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val testUsername = "ui_test_user"
    private val testPassword = "password"
    private val testUserId = 9_999L

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sessionManager = SessionManager(context)
        sessionManager.clearSession()

        db =
            Room
                .databaseBuilder(context, AppDatabase::class.java, "sports_organizer.db")
                .fallbackToDestructiveMigration()
                .build()

        runBlocking {
            val user =
                User(
                    id = testUserId,
                    firstName = "UiTest",
                    lastName = "User",
                    username = testUsername,
                    password = testPassword,
                )
            db.userDao().insertAll(user)
        }

        sessionManager.saveLoggedInUserId(testUserId)
    }

    @After
    fun tearDown() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SessionManager(context).clearSession()
        db.close()
    }

    @Test
    fun loggedIn_user_seesYourCompetitions_header_onOrganize() {
        composeTestRule.onNodeWithText("Organize").performClick()

        composeTestRule.onNodeWithText("Your competitions").assertIsDisplayed()
    }
}
