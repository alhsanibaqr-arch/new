package com.vardely.app

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUiTest {
    @Test
    fun navigationAndAccessibilityDisclosureAreVisible() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withText(R.string.home_title)).check(matches(isDisplayed()))

            onView(withTagValue(`is`("nav_apps"))).perform(click())
            onView(withText(R.string.apps_title)).check(matches(isDisplayed()))

            onView(withTagValue(`is`("nav_stats"))).perform(click())
            onView(withText(R.string.stats_title)).check(matches(isDisplayed()))

            onView(withTagValue(`is`("nav_settings"))).perform(click())
            onView(withText(R.string.app_lock_settings)).check(matches(isDisplayed()))

            onView(withTagValue(`is`("nav_home"))).perform(click())
            onView(withText(R.string.enable_protection)).perform(click())
            onView(withText(R.string.accessibility_disclosure_title)).check(matches(isDisplayed()))
            onView(withText(R.string.accessibility_disclosure_body)).check(matches(isDisplayed()))
        }
    }
}
