package com.cyberity.cvsu

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Locale

// ===========================================================================
// USER PROFILE
// ===========================================================================
// One document per account in "users/{uid}", holding who the student is:
// their student ID and the name shown to other students (e.g. on the
// leaderboard). Game progress stays in "userProgress/{uid}" — the two are kept
// apart so the profile can later be made readable by others without exposing
// hearts, XP spending, etc.
//
// Accounts created before this existed have no profile yet. After login,
// AppNavigator checks isComplete and shows CompleteProfileScreen once.
//
// Student IDs are unique: "studentIds/{id}" records which account owns an ID,
// claimed in a transaction so two accounts can't register the same one.

data class UserProfile(
    val studentId: String,
    val displayName: String,
    val isTester: Boolean = false,
    val hasCompletedOnboarding: Boolean = false
) {
    val isComplete: Boolean get() = studentId.isNotBlank() && displayName.isNotBlank()
}

object StudentIdRules {

    /**
     * CvSU student ID: 9 digits, the first 4 being the year of entry,
     * e.g. 202310502 (2023 + 10502).
     */
    private val STUDENT_ID = Regex("^\\d{9}$")

    /** Earliest year of entry accepted; the latest is the current year. */
    private const val MIN_ENTRY_YEAR = 2000

    /** Developer/test accounts use IDs like DEV-001. Only accepted in debug builds. */
    private val DEV_ID = Regex("^DEV-\\d{1,4}$")

    const val DISPLAY_NAME_MIN = 3
    const val DISPLAY_NAME_MAX = 20

    fun normalize(input: String): String = input.trim().uppercase(Locale.ROOT)

    fun isDevId(id: String): Boolean = DEV_ID.matches(normalize(id))

    /** Null when valid, otherwise the message to show under the field. */
    fun validateStudentId(input: String, debugBuild: Boolean): String? {
        val id = normalize(input)
        return when {
            id.isEmpty() -> "Please enter your student ID"
            isDevId(id) -> if (debugBuild) null else "Please enter a valid student ID"
            !STUDENT_ID.matches(id) -> "Student ID must be 9 digits, e.g. 202310502"
            id.take(4).toInt() !in MIN_ENTRY_YEAR..currentYear() ->
                "Student ID should start with your year of entry, e.g. 2023"
            else -> null
        }
    }

    private fun currentYear(): Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

    fun validateDisplayName(input: String): String? {
        val name = input.trim()
        return when {
            name.length < DISPLAY_NAME_MIN -> "Name must be at least $DISPLAY_NAME_MIN characters"
            name.length > DISPLAY_NAME_MAX -> "Name must be at most $DISPLAY_NAME_MAX characters"
            else -> null
        }
    }
}

/** True for builds run from Android Studio; false for release builds given to students. */
fun Context.isDebugBuild(): Boolean =
    (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

sealed interface ProfileSaveResult {
    data object Saved : ProfileSaveResult
    data object IdTaken : ProfileSaveResult
    data class Failed(val message: String) : ProfileSaveResult
}

object UserProfileRepository {

    private val db by lazy { FirebaseFirestore.getInstance() }

    private const val USERS = "users"
    private const val STUDENT_IDS = "studentIds"

    private const val FIELD_STUDENT_ID = "studentId"
    private const val FIELD_DISPLAY_NAME = "displayName"
    private const val FIELD_EMAIL = "email"
    private const val FIELD_IS_TESTER = "isTester"
    private const val FIELD_UID = "uid"
    private const val FIELD_HAS_COMPLETED_ONBOARDING = "hasCompletedOnboarding"
    private const val FIELD_UPDATED_AT = "updatedAt"

    /**
     * Loads the profile for [uid]. Calls [onResult] with null when the account
     * has no profile yet, and [onError] when Firestore couldn't be reached.
     */
    fun load(uid: String, onResult: (UserProfile?) -> Unit, onError: (String) -> Unit) {
        Log.d("CYBERITY_ONBOARDING", "LOOKING FOR STUDENT DOCUMENT FOR UID: $uid")
        db.collection(USERS).document(uid).get()
            .addOnSuccessListener { snapshot ->
                val id = snapshot.getString(FIELD_STUDENT_ID)
                val name = snapshot.getString(FIELD_DISPLAY_NAME)

                if (id != null && name != null) {
                    val userHasOnboarded = snapshot.getBoolean(FIELD_HAS_COMPLETED_ONBOARDING)
                    Log.d("CYBERITY_ONBOARDING", "STUDENT DOCUMENT FOUND: users/$uid (studentId: $id)")
                    Log.d("CYBERITY_ONBOARDING", "READING hasCompletedOnboarding from users/$uid")

                    if (userHasOnboarded != null) {
                        Log.d("CYBERITY_ONBOARDING", "hasCompletedOnboarding VALUE: $userHasOnboarded")
                        onResult(UserProfile(id, name, snapshot.getBoolean(FIELD_IS_TESTER) ?: false, userHasOnboarded))
                    } else {
                        // Check if studentIds/{studentId} has the field
                        db.collection(STUDENT_IDS).document(id).get()
                            .addOnSuccessListener { studentDoc ->
                                val studentHasOnboarded = studentDoc.getBoolean(FIELD_HAS_COMPLETED_ONBOARDING) ?: false
                                Log.d("CYBERITY_ONBOARDING", "hasCompletedOnboarding VALUE from studentIds/$id: $studentHasOnboarded")
                                onResult(UserProfile(id, name, snapshot.getBoolean(FIELD_IS_TESTER) ?: false, studentHasOnboarded))
                            }
                            .addOnFailureListener {
                                Log.d("CYBERITY_ONBOARDING", "hasCompletedOnboarding FIELD MISSING - DEFAULTING TO FALSE")
                                onResult(UserProfile(id, name, snapshot.getBoolean(FIELD_IS_TESTER) ?: false, false))
                            }
                    }
                } else {
                    // Try searching in studentIds where uid == uid
                    db.collection(STUDENT_IDS).whereEqualTo("uid", uid).get()
                        .addOnSuccessListener { query ->
                            val doc = query.documents.firstOrNull()
                            if (doc != null) {
                                val sId = doc.id
                                val sName = doc.getString(FIELD_DISPLAY_NAME) ?: id ?: "Student"
                                val hasOnboarded = doc.getBoolean(FIELD_HAS_COMPLETED_ONBOARDING) ?: false
                                Log.d("CYBERITY_ONBOARDING", "STUDENT DOCUMENT FOUND in studentIds/$sId")
                                Log.d("CYBERITY_ONBOARDING", "hasCompletedOnboarding VALUE: $hasOnboarded")
                                onResult(UserProfile(sId, sName, false, hasOnboarded))
                            } else {
                                Log.e("CYBERITY_ONBOARDING", "STUDENT DOCUMENT NOT FOUND for $uid")
                                onResult(null)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("CYBERITY_ONBOARDING", "FIRESTORE ERROR: ${e.message}", e)
                            onError(e.localizedMessage ?: e.toString())
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("CYBERITY_ONBOARDING", "FIRESTORE ERROR: ${e.message}", e)
                onError(e.localizedMessage ?: e.toString())
            }
    }

    /**
     * Marks onboarding completed in Firestore using a field-level merge update.
     * Does NOT overwrite existing user fields.
     */
    fun setOnboardingCompleted(uid: String, studentId: String? = null, onComplete: (Boolean, Exception?) -> Unit = { _, _ -> }) {
        Log.d("CYBERITY_FIRESTORE", "Student document found for UID: $uid, studentId: $studentId")
        Log.d("CYBERITY_FIRESTORE", "Updating hasCompletedOnboarding to true...")

        val updates = mapOf(FIELD_HAS_COMPLETED_ONBOARDING to true)

        db.collection(USERS).document(uid)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("CYBERITY_FIRESTORE", "Firestore onboarding update SUCCESS on users/$uid")

                if (!studentId.isNullOrBlank()) {
                    db.collection(STUDENT_IDS).document(studentId)
                        .set(updates, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("CYBERITY_FIRESTORE", "Firestore onboarding update SUCCESS on studentIds/$studentId")
                            onComplete(true, null)
                        }
                        .addOnFailureListener { e ->
                            Log.e("CYBERITY_FIRESTORE", "Firestore onboarding update FAILED on studentIds/$studentId", e)
                            onComplete(true, e)
                        }
                } else {
                    db.collection(STUDENT_IDS).whereEqualTo("uid", uid).get()
                        .addOnSuccessListener { query ->
                            for (doc in query.documents) {
                                doc.reference.set(updates, SetOptions.merge())
                                    .addOnSuccessListener {
                                        Log.d("CYBERITY_FIRESTORE", "Firestore onboarding update SUCCESS on studentIds/${doc.id}")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("CYBERITY_FIRESTORE", "Firestore onboarding update FAILED on studentIds/${doc.id}", e)
                                    }
                            }
                            onComplete(true, null)
                        }
                        .addOnFailureListener {
                            onComplete(true, null)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("CYBERITY_FIRESTORE", "Firestore onboarding update FAILED on users/$uid", e)
                onComplete(false, e)
            }
    }

    /**
     * Claims [profile].studentId for [uid] and writes the profile, atomically.
     * Re-saving your own ID is fine; an ID owned by another account is IdTaken.
     * If the student changes their ID, the old claim is released.
     */
    fun save(
        uid: String,
        email: String?,
        profile: UserProfile,
        onResult: (ProfileSaveResult) -> Unit
    ) {
        val userDoc = db.collection(USERS).document(uid)
        val newIdDoc = db.collection(STUDENT_IDS).document(profile.studentId)

        db.runTransaction { tx ->
            val owner = tx.get(newIdDoc).getString(FIELD_UID)
            if (owner != null && owner != uid) {
                return@runTransaction false
            }
            val previousId = tx.get(userDoc).getString(FIELD_STUDENT_ID)

            if (previousId != null && previousId != profile.studentId) {
                tx.delete(db.collection(STUDENT_IDS).document(previousId))
            }
            tx.set(newIdDoc, mapOf(FIELD_UID to uid, FIELD_HAS_COMPLETED_ONBOARDING to profile.hasCompletedOnboarding), SetOptions.merge())
            tx.set(
                userDoc,
                mapOf(
                    FIELD_STUDENT_ID to profile.studentId,
                    FIELD_DISPLAY_NAME to profile.displayName,
                    FIELD_EMAIL to email,
                    FIELD_IS_TESTER to profile.isTester,
                    FIELD_HAS_COMPLETED_ONBOARDING to profile.hasCompletedOnboarding,
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            true
        }
            .addOnSuccessListener { claimed ->
                onResult(if (claimed) ProfileSaveResult.Saved else ProfileSaveResult.IdTaken)
            }
            .addOnFailureListener { e ->
                Log.e("UserProfile", "Could not save profile for $uid", e)
                onResult(ProfileSaveResult.Failed(e.localizedMessage ?: "Could not save your profile"))
            }
    }
}

/**
 * Same-device record that this account's profile is complete, so a student
 * who already filled it in isn't blocked by the check when they're offline.
 */
object ProfileCache {

    private const val PREFS_NAME = "cyberity_profile_cache"

    fun isComplete(context: Context, uid: String): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(uid, false)

    fun markComplete(context: Context, uid: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(uid, true)
            .apply()
    }
}