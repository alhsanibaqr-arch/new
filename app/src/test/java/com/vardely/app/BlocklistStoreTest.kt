package com.vardely.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class BlocklistStoreTest {
    private lateinit var store: BlocklistStore

    @Before
    fun setUp() {
        val app = RuntimeEnvironment.getApplication()
        app.getSharedPreferences("vardely_preferences", 0).edit().clear().commit()
        store = BlocklistStore(app)
    }

    @Test
    fun pinIsStoredAsSaltedHashAndVerifiesOnlyMatchingCode() {
        store.setPin("4826")

        assertTrue(store.hasPin())
        assertTrue(store.verifyPin("4826"))
        assertFalse(store.verifyPin("4827"))
        val values = RuntimeEnvironment.getApplication()
            .getSharedPreferences("vardely_preferences", 0).all
        assertFalse(values.containsValue("4826"))
        assertTrue(values.containsKey("pin_salt"))
        assertTrue(values.containsKey("pin_hash"))
    }

    @Test
    fun blockAndLockSelectionsRemainIndependent() {
        store.setAppBlocked("example.blocked", true)
        store.setAppLocked("example.locked", true)
        store.setBlockingEnabled(false)

        assertTrue(store.blockedPackages().contains("example.blocked"))
        assertFalse(store.blockedPackages().contains("example.locked"))
        assertTrue(store.lockedPackages().contains("example.locked"))
        assertFalse(store.isBlockingEnabled())
    }

    @Test
    fun blockedAttemptCountsStaySeparatedByDate() {
        store.recordBlockedAttempt("2026-10-14")
        store.recordBlockedAttempt("2026-10-14")
        store.recordBlockedAttempt("2026-10-13")

        assertEquals(2, store.attemptsForDate("2026-10-14"))
        assertEquals(1, store.attemptsForDate("2026-10-13"))
        assertEquals(0, store.attemptsForDate("2026-10-12"))
    }

    @Test
    fun accessibilityDisclosureMustBeExplicitlyAccepted() {
        assertFalse(store.isDisclosureAccepted())
        store.setDisclosureAccepted(true)
        assertTrue(store.isDisclosureAccepted())
    }
}
