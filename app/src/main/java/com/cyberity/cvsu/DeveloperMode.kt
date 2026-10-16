package com.cyberity.cvsu

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges

/** Administrators grant tester access through users/{uid}.isTester in Firebase Console. */
internal object DeveloperModePolicy {
    fun unlockAllLevels(data: Map<String, Any>?, fromCache: Boolean, pendingWrites: Boolean): Boolean =
        !fromCache && !pendingWrites && data?.get("isTester") == true
}

internal object DeveloperModeRepository {
    /** Recheck a sample export after returning from the system file picker. */
    fun confirm(uid: String): com.google.android.gms.tasks.Task<Boolean> =
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .get(com.google.firebase.firestore.Source.SERVER).continueWith {
                val snapshot = it.result
                FirebaseAuth.getInstance().currentUser?.uid == uid && DeveloperModePolicy.unlockAllLevels(
                    snapshot.data, snapshot.metadata.isFromCache, snapshot.metadata.hasPendingWrites())
            }

    fun listen(uid: String?, onChanged: (Boolean) -> Unit): ListenerRegistration? {
        onChanged(false)
        if (uid == null) return null
        return FirebaseFirestore.getInstance().collection("users").document(uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                onChanged(error == null && FirebaseAuth.getInstance().currentUser?.uid == uid &&
                    snapshot != null && DeveloperModePolicy.unlockAllLevels(
                        snapshot.data, snapshot.metadata.isFromCache, snapshot.metadata.hasPendingWrites()))
                if (error == null && snapshot != null && !snapshot.metadata.isFromCache &&
                    FirebaseAuth.getInstance().currentUser?.uid == uid) LeaderboardRepository.publish(uid)
            }
    }
}
