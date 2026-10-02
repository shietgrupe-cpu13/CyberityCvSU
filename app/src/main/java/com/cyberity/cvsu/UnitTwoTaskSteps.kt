package com.cyberity.cvsu

/** Concrete actions in each simulated case, following the Unit 1 / Unit 3 task format. */
fun unitTwoTaskSteps(levelId: Int, index: Int): List<String> {
    val actions = when (levelId) {
        201 -> listOf(
            listOf("Ask **Maya** where else she uses her old password. Inspect the portal credential.", "Choose **20 or 24 characters**, generate a replacement, and confirm that it belongs only to the portal."),
            listOf("Check that Maya's **portal replacement** was carried forward from Task 1.", "Replace the **shopping credential** with a different generated value and choose a **protected password manager**."),
            listOf("Read Maya's account of the **monthly reset policy**.", "Allow long passwords and autofill, block common or breached values, and reset when **compromise is suspected or confirmed**.")
        )
        202 -> listOf(
            listOf("Ask **Jules** about the alert, then open **login evidence**.", "Identify how the guesses were chosen. Record the attack type and apply **online throttling** and common-password blocking."),
            listOf("Inspect the **leaked credential pairs** and the successful sessions.", "Record the attack type. Reset affected credentials, remove reuse, add sign-in protection, and review **successful access**."),
            listOf("Ask Jules why portal login limits cannot protect the **copied database**.", "Calculate both classroom digests. Change an input and recalculate to compare the outputs.", "Select **slow salted storage** and reset the exposed credentials.")
        )
        203 -> listOf(
            listOf("Ask **Maya** which device she controls. Distinguish a possession factor from another knowledge secret.", "Choose the authenticator or security key, **enrol it**, and enter the displayed **training proof**."),
            listOf("Confirm whether Maya started the **approval requests**.", "Deny the requests, inspect **official login history**, and record the suspicious attempt."),
            listOf("Register a **passkey through the official portal**.", "Choose private, protected storage for its **recovery codes**.")
        )
        204 -> listOf(
            listOf("Ask Maya who controls the **recovery mailbox** and who can read her backup codes.", "Choose a current protected mailbox, **replace exposed codes**, and store the replacements privately."),
            listOf("Compare the **trusted personal phone** with the abandoned **library session**.", "Revoke the library session, keep the phone active, and choose **sign-out** for shared devices."),
            listOf("Read what changed after the **unfamiliar login**.", "Replace the credential, revoke attacker access, restore recovery, remove forwarding, and record the incident.")
        )
        250 -> listOf(
            listOf("Ask **Elena** about the authorised common-word audit. Inspect the **salt and target digest**.", "Start the round, load the **dictionary**, and calculate the candidate hashes."),
            listOf("Inspect this new record's **salt, target, and reference year**.", "Start the round and load the **campus word + year + suffix** candidates."),
            listOf("Inspect the two-digit record and keep **leading zeroes**.", "Start the round and calculate **00 through 99** as two-character strings.")
        )
        205 -> return emptyList() // Capstone: apply the earlier skills without prescribed repairs.
        else -> error("Unknown Unit 2 level: $levelId")
    }
    val finish = if (levelId == 250)
        "Compare the **entire digest** yourself and submit the candidate that matches. Retries start a fresh record."
    else "Use **Save account changes** to verify your investigation or repair."
    return listOf("Open the simulation and read the person's request for this task.") + actions[index] +
        listOf(finish, "Read the outcome. Return to the task panel and submit the **CASE code** earned by this task.")
}
