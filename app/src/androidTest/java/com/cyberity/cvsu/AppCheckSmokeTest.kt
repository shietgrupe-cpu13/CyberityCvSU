package com.cyberity.cvsu

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AppCheckSmokeTest {
    @Test fun registeredProviderObtainsToken() {
        val result = Tasks.await(FirebaseAppCheck.getInstance().getAppCheckToken(true), 45, TimeUnit.SECONDS)
        assertTrue("App Check must return a token", result.token.isNotBlank())
    }

    @Test fun signedInPhoneReadsItsProfileAndProgress() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        assertNotNull("Keep the test phone signed in", uid)
        val db = FirebaseFirestore.getInstance()
        Tasks.await(db.collection("users").document(uid!!).get(Source.SERVER), 30, TimeUnit.SECONDS)
        val progress = Tasks.await(db.collection("userProgress").document(uid).get(Source.SERVER), 30, TimeUnit.SECONDS)
        val xp = ScorePolicy.balance(progress.get("levelXp") as? Map<*, *>, progress.getLong("xpSpent") ?: 0)
        assertTrue(xp in 0..(STARTING_XP + ScorePolicy.levelIds.size * MAX_LEVEL_XP))
    }

    @Test fun publicScoreSynchronizesFromStoredProgress() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        assertNotNull("Keep the phone signed in", uid)
        Tasks.await(LeaderboardRepository.publish(uid!!), 30, TimeUnit.SECONDS)
        val db = FirebaseFirestore.getInstance()
        val privacy = Tasks.await(db.collection("privacyPreferences").document(uid).get(Source.SERVER), 30, TimeUnit.SECONDS)
        if (privacy.getBoolean("leaderboardVisible") == false) return
        val progress = Tasks.await(db.collection("userProgress").document(uid).get(Source.SERVER), 30, TimeUnit.SECONDS)
        val profile = Tasks.await(db.collection("users").document(uid).get(Source.SERVER), 30, TimeUnit.SECONDS)
        val board = Tasks.await(db.collection("leaderboard").document(uid).get(Source.SERVER), 30, TimeUnit.SECONDS)
        assertEquals(ScorePolicy.balance(progress.get("levelXp") as? Map<*, *>, progress.getLong("xpSpent") ?: 0).toLong(), board.getLong("xp"))
        assertEquals(profile.getString("displayName"), board.getString("displayName"))
        assertEquals(profile.getBoolean("isTester") ?: false, board.getBoolean("isTester"))
    }
}
