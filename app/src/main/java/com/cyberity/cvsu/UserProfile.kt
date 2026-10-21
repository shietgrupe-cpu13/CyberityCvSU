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
    val hasCompletedOnboarding: Boolean = false,
    val yearLevel: Int? = null,
    val fullName: String = "",
    val usernameChangeRequired: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = ""
) {
    val certificateName: String get() = if (firstName.isNotBlank() && lastName.isNotBlank())
        StudentIdRules.composeFullName(firstName, middleName, lastName) else fullName
    val isComplete: Boolean get() = studentId.isNotBlank() && displayName.isNotBlank() && !usernameChangeRequired
}

object StudentIdRules {

    fun composeFullName(firstName: String, middleName: String, lastName: String): String =
        listOf(firstName.trim(), middleName.trim(), lastName.trim()).filter { it.isNotEmpty() }.joinToString(" ")

    fun validateNameParts(firstName: String, middleName: String, lastName: String): String? {
        for ((label, value) in listOf("First name" to firstName, "Last name" to lastName, "Middle name" to middleName)) {
            if (label != "Middle name" && value.isBlank()) return "$label is required"
            if (value.trim().length > 50) return "$label must be at most 50 characters"
            if (value.any { it.isISOControl() }) return "$label cannot contain control characters"
        }
        return validateFullName(composeFullName(firstName, middleName, lastName))
    }

    fun validateFullName(input: String): String? = when {
        input.trim().length !in 2..100 -> "Full name must be between 2 and 100 characters"
        input.any { it.isISOControl() } -> "Full name cannot contain control characters"
        else -> null
    }

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

    fun usernameKey(input: String): String = input.trim().lowercase(Locale.ROOT)

    fun validateDisplayName(input: String): String? {
        val name = input.trim()
        return when {
            name.contains('/') || name.any { it.isISOControl() } -> "Username cannot contain slashes or control characters"
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
    data object UsernameTaken : ProfileSaveResult
    data class Failed(val message: String) : ProfileSaveResult
}

object UserProfileRepository {

    private val db by lazy { FirebaseFirestore.getInstance() }

    private const val USERS = "users"
    private const val STUDENT_IDS = "studentIds"

    private const val FIELD_YEAR_LEVEL = "yearLevel"
    private const val FIELD_STUDENT_ID = "studentId"
    private const val FIELD_DISPLAY_NAME = "displayName"
    private const val FIELD_EMAIL = "email"
    private const val FIELD_IS_TESTER = "isTester"
    private const val FIELD_UID = "uid"
    private const val FIELD_HAS_COMPLETED_ONBOARDING = "hasCompletedOnboarding"
    private const val FIELD_UPDATED_AT = "updatedAt"

    /** Server-confirmed identity for certificate export; never issue from a stale cache. */
    fun loadConfirmed(uid: String): com.google.android.gms.tasks.Task<UserProfile?> =
        db.collection(USERS).document(uid).get(com.google.firebase.firestore.Source.SERVER).continueWith {
            val document = it.result
            val id = document.getString(FIELD_STUDENT_ID)
            val name = document.getString(FIELD_DISPLAY_NAME)
            if (id == null || name == null) null else UserProfile(id, name,
                document.getBoolean(FIELD_IS_TESTER) ?: false,
                document.getBoolean(FIELD_HAS_COMPLETED_ONBOARDING) ?: false,
                YearLevels.fromStored(document.get(FIELD_YEAR_LEVEL)), document.getString("fullName").orEmpty(), document.getBoolean("usernameChangeRequired") ?: false, document.getString("firstName").orEmpty(), document.getString("lastName").orEmpty(), document.getString("middleName").orEmpty())
        }

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
                        onResult(UserProfile(id, name, snapshot.getBoolean(FIELD_IS_TESTER) ?: false, userHasOnboarded, YearLevels.fromStored(snapshot.get(FIELD_YEAR_LEVEL)), snapshot.getString("fullName").orEmpty(), snapshot.getBoolean("usernameChangeRequired") ?: false, snapshot.getString("firstName").orEmpty(), snapshot.getString("lastName").orEmpty(), snapshot.getString("middleName").orEmpty()))
                    } else {
                        // Check if studentIds/{studentId} has the field
                        db.collection(STUDENT_IDS).document(id).get()
                            .addOnSuccessListener { studentDoc ->
                                val studentHasOnboarded = studentDoc.getBoolean(FIELD_HAS_COMPLETED_ONBOARDING) ?: false
                                Log.d("CYBERITY_ONBOARDING", "hasCompletedOnboarding VALUE from studentIds/$id: $studentHasOnboarded")
                                onResult(UserProfile(id, name, snapshot.getBoolean(FIELD_IS_TESTER) ?: false, studentHasOnboarded, YearLevels.fromStored(snapshot.get(FIELD_YEAR_LEVEL)), snapshot.getString("fullName").orEmpty(), snapshot.getBoolean("usernameChangeRequired") ?: false, snapshot.getString("firstName").orEmpty(), snapshot.getString("lastName").orEmpty(), snapshot.getString("middleName").orEmpty()))
                            }
                            .addOnFailureListener {
                                Log.d("CYBERITY_ONBOARDING", "hasCompletedOnboarding FIELD MISSING - DEFAULTING TO FALSE")
                                onResult(UserProfile(id, name, snapshot.getBoolean(FIELD_IS_TESTER) ?: false, false, YearLevels.fromStored(snapshot.get(FIELD_YEAR_LEVEL)), snapshot.getString("fullName").orEmpty(), snapshot.getBoolean("usernameChangeRequired") ?: false, snapshot.getString("firstName").orEmpty(), snapshot.getString("lastName").orEmpty(), snapshot.getString("middleName").orEmpty()))
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
                                onResult(UserProfile(sId, sName, false, hasOnboarded, YearLevels.fromStored(doc.get(FIELD_YEAR_LEVEL)), doc.getString("fullName").orEmpty()))
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

    fun changeUsername(uid: String, input: String): com.google.android.gms.tasks.Task<String> {
        val name = input.trim()
        StudentIdRules.validateDisplayName(name)?.let { throw IllegalArgumentException(it) }
        val user = db.collection(USERS).document(uid)
        val key = StudentIdRules.usernameKey(name)
        val claim = db.collection("usernames").document(key)
        return db.runTransaction { tx ->
            val profile = tx.get(user)
            val existingClaim = tx.get(claim)
            val oldKey = profile.getString("usernameKey")
            val oldClaim = oldKey?.takeIf { it != key }?.let { db.collection("usernames").document(it) }
            val oldOwner = oldClaim?.let { tx.get(it).getString("uid") }
            check(profile.exists()) { "Your profile is unavailable." }
            check(name != profile.getString("displayName")) { "Choose a different username." }
            val changedAt = profile.getTimestamp("usernameChangedAt")
            if (profile.getBoolean("usernameChangeRequired") != true && changedAt != null) {
                val next = changedAt.toDate().time + 30L * 24 * 60 * 60 * 1000
                check(System.currentTimeMillis() >= next) { "You can change your username again on " +
                    java.text.SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(java.util.Date(next)) + "." }
            }
            check(existingClaim.getString("uid") == null || existingClaim.getString("uid") == uid) { "This username is already taken. Choose another." }
            tx.set(claim, mapOf("uid" to uid))
            tx.update(user, mapOf("displayName" to name, "usernameKey" to key,
                "usernameChangeRequired" to false, "usernameChangedAt" to FieldValue.serverTimestamp(),
                FIELD_UPDATED_AT to FieldValue.serverTimestamp()))
            if (oldClaim != null && oldOwner == uid) tx.delete(oldClaim)
            // Never leave a public row showing the previous name.
            tx.delete(db.collection("leaderboard").document(uid))
            name
        }
    }

    fun updateStudentInformation(uid: String, firstName: String, middleName: String, lastName: String, year: Int): com.google.android.gms.tasks.Task<Void> {
        require(StudentIdRules.validateNameParts(firstName, middleName, lastName) == null)
        require(YearLevels.isValid(year))
        return db.collection(USERS).document(uid).update(mapOf(
            "firstName" to firstName.trim(), "middleName" to middleName.trim(), "lastName" to lastName.trim(),
            "fullName" to StudentIdRules.composeFullName(firstName, middleName, lastName), FIELD_YEAR_LEVEL to year,
            FIELD_UPDATED_AT to FieldValue.serverTimestamp()))
    }

    /** Updates only the year field so older accounts keep their identity and progress. */
    fun updateYearLevel(uid: String, year: Int): com.google.android.gms.tasks.Task<Void> {
        require(YearLevels.isValid(year))
        return db.collection(USERS).document(uid).update(mapOf(
            FIELD_YEAR_LEVEL to year, FIELD_UPDATED_AT to FieldValue.serverTimestamp()))
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
        if (!YearLevels.isValid(profile.yearLevel)) {
            onResult(ProfileSaveResult.Failed("Please select your year level"))
            return
        }
        StudentIdRules.validateNameParts(profile.firstName, profile.middleName, profile.lastName)?.let {
            onResult(ProfileSaveResult.Failed(it)); return
        }
        StudentIdRules.validateDisplayName(profile.displayName)?.let {
            onResult(ProfileSaveResult.Failed(it)); return
        }
        val userDoc = db.collection(USERS).document(uid)
        val usernameKey = StudentIdRules.usernameKey(profile.displayName)
        val usernameDoc = db.collection("usernames").document(usernameKey)
        val newIdDoc = db.collection(STUDENT_IDS).document(profile.studentId)

        db.runTransaction { tx ->
            val owner = tx.get(newIdDoc).getString(FIELD_UID)
            if (owner != null && owner != uid) {
                return@runTransaction ProfileSaveResult.IdTaken
            }
            val usernameOwner = tx.get(usernameDoc).getString(FIELD_UID)
            if (usernameOwner != null && usernameOwner != uid) {
                return@runTransaction ProfileSaveResult.UsernameTaken
            }
            val previousProfile = tx.get(userDoc)
            val previousUsername = previousProfile.getString("usernameKey")
            if (previousProfile.exists() && previousProfile.getString(FIELD_DISPLAY_NAME) != profile.displayName) {
                tx.update(userDoc, mapOf("usernameChangedAt" to FieldValue.serverTimestamp()))
            }
            // All reads precede writes; the claim and profile commit together.
            if (previousUsername != null && previousUsername != usernameKey) {
                tx.delete(db.collection("usernames").document(previousUsername))
            }
            tx.set(usernameDoc, mapOf(FIELD_UID to uid))
            val previousId = previousProfile.getString(FIELD_STUDENT_ID)

            if (previousId != null && previousId != profile.studentId) {
                tx.delete(db.collection(STUDENT_IDS).document(previousId))
            }
            if (owner == null) tx.set(newIdDoc, mapOf(FIELD_UID to uid, FIELD_HAS_COMPLETED_ONBOARDING to (previousProfile.getBoolean(FIELD_HAS_COMPLETED_ONBOARDING) ?: profile.hasCompletedOnboarding)), SetOptions.merge())
            tx.set(
                userDoc,
                mapOf(
                    "usernameKey" to usernameKey,
                    "usernameChangeRequired" to false,
                    "fullName" to profile.certificateName.trim(),
                    "firstName" to profile.firstName.trim(),
                    "middleName" to profile.middleName.trim(),
                    "lastName" to profile.lastName.trim(),
                    FIELD_YEAR_LEVEL to profile.yearLevel,
                    FIELD_STUDENT_ID to profile.studentId,
                    FIELD_DISPLAY_NAME to profile.displayName,
                    FIELD_EMAIL to email,
                    FIELD_IS_TESTER to (previousProfile.getBoolean(FIELD_IS_TESTER) ?: false),
                    FIELD_HAS_COMPLETED_ONBOARDING to (previousProfile.getBoolean(FIELD_HAS_COMPLETED_ONBOARDING) ?: profile.hasCompletedOnboarding),
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            ProfileSaveResult.Saved
        }
            .addOnSuccessListener { claimed ->
                onResult(claimed)
            }
            .addOnFailureListener { e ->
                Log.e("UserProfile", "Could not save profile for $uid", e)
                // A competing commit can be denied by rules before the SDK retries.
                // Recheck ownership so a collision still gets the actionable message.
                usernameDoc.get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener { claim ->
                        val owner = claim.getString(FIELD_UID)
                        onResult(if (owner != null && owner != uid) ProfileSaveResult.UsernameTaken
                            else ProfileSaveResult.Failed(e.localizedMessage ?: "Could not save your profile"))
                    }
                    .addOnFailureListener {
                        onResult(ProfileSaveResult.Failed("Couldn't save your profile. Check your connection and try again."))
                    }
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

    /** Name and student ID for the Profile tab, so it shows them on its first frame. */
    fun saveDetails(context: Context, uid: String, name: String, studentId: String, yearLevel: Int? = null) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("$uid:name", name)
            .putString("$uid:studentId", studentId)
            .also { if (yearLevel != null) it.putInt("$uid:yearLevel", yearLevel) }
            .apply()
    }

    fun loadYearLevel(context: Context, uid: String): Int? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt("$uid:yearLevel", 0).takeIf { YearLevels.isValid(it) }

    fun loadName(context: Context, uid: String): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("$uid:name", null)

    fun loadStudentId(context: Context, uid: String): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("$uid:studentId", null)
}
