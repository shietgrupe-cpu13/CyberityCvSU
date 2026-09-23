package com.cyberity.cvsu

// ===========================================================================
// LEVEL 304 CONTENT — Scam Messages, on the student's own phone
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same format as 101-102:
// every task teaches first (guide), says exactly what to do (steps), then asks.
//
// 301-303 put the student behind a desk looking at other people's problems.
// 304 hands them their own phone: four SMS threads and one recorded call,
// written the way these actually arrive in the Philippines.

/** Clue ids reported by the phone simulation through the JS bridge. */
object ScamMsgClues {
    const val THREAD_OPENED_S1 = "thread_opened_s1"
    const val THREAD_OPENED_S2 = "thread_opened_s2"
    const val THREAD_OPENED_S3 = "thread_opened_s3"
    const val THREAD_OPENED_S4 = "thread_opened_s4"
    const val MESSAGE_DETAILS_S1 = "message_details_s1"
    const val PARCEL_PAGE_OPENED = "parcel_page_opened"
    const val PARCEL_PAGE_INSPECTED = "parcel_page_inspected"
    const val GCASH_PAGE_OPENED = "gcash_page_opened"
    const val CALL_PLAYED = "call_played"

    /** Dangerous: replying to a scam text, which confirms the number is live. */
    const val REPLIED_TO_SCAM = "replied_to_scam"

    /** Dangerous: typing card details into the fake courier payment page. */
    const val CARD_ENTERED = "card_entered"
}

val scamMsgClueLabels: Map<String, String> = mapOf(
    ScamMsgClues.THREAD_OPENED_S1 to "Read the GCASH thread",
    ScamMsgClues.THREAD_OPENED_S2 to "Read the raffle prize text",
    ScamMsgClues.THREAD_OPENED_S3 to "Read the parcel-on-hold text",
    ScamMsgClues.THREAD_OPENED_S4 to "Read the home-based job offer",
    ScamMsgClues.MESSAGE_DETAILS_S1 to "Opened message details in the GCASH thread",
    ScamMsgClues.PARCEL_PAGE_OPENED to "Opened the courier link in the sandbox",
    ScamMsgClues.PARCEL_PAGE_INSPECTED to "Inspected the courier page's code",
    ScamMsgClues.GCASH_PAGE_OPENED to "Opened the wallet link in the sandbox",
    ScamMsgClues.CALL_PLAYED to "Played the recorded call to the end"
)

fun scamMessagesLab(): LabDefinition = LabDefinition(
    levelId = 304,
    title = "Your Phone, One Week",
    subtitle = "Scam Messages · smishing and vishing",
    briefing = "Phishing by text message is called smishing; by phone call, vishing. In the " +
            "Philippines these are the versions almost everyone meets first — a prize you " +
            "never entered, a parcel you never ordered, a job that pays too well, a bank " +
            "officer who needs your OTP right now.\n\n" +
            "This is your own phone for one week: four message threads and one recorded call. " +
            "Nothing here can send or dial anything. Replying, tapping a link and opening a " +
            "page are all safe — but typing your details into one of these pages, or replying " +
            "to a scam, costs a heart, exactly as it would cost you in real life.\n\n" +
            "Each task explains what to look for, then hands you the phone. Open it with the " +
            "button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "scam_messages",
    startPage = "messages.html",
    dangerousClues = listOf(
        ScamMsgClues.REPLIED_TO_SCAM,
        ScamMsgClues.CARD_ENTERED
    ),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Same name, same thread",
            objective = "Open the GCASH thread. The last message sits under the same name as " +
                    "your real receipts. Open its details and decide what that proves.",
            guide = listOf(
                "Banks and wallets send texts under a name instead of a number — GCASH, BDO, " +
                        "BPI. That name is called a sender ID, and your phone groups every " +
                        "message carrying the same sender ID into one thread. It looks like a " +
                        "conversation with the company.",
                "It isn't. A sender ID is just a label attached to the message as it travels, " +
                        "and scammers buy access to networks that let them set it to anything " +
                        "they like. When they set it to GCASH, your phone files their message in " +
                        "the same thread as your genuine receipts, right underneath them. " +
                        "Nothing about that placement is verified.",
                "This is why \"it came in the same thread as the real ones\" is not a safety " +
                        "check. It is the single most convincing trick in Philippine smishing, " +
                        "and the only defence is to judge each message by what it asks you to " +
                        "do — never by where it landed."
            ),
            steps = listOf(
                "Open the phone and tap the GCASH thread.",
                "Read the three messages. The first two are ordinary receipts.",
                "Long-press the last message and choose DETAILS.",
                "Compare how the genuine receipts arrived with how the last one arrived.",
                "Swipe the bar at the top down and choose what the thread placement proves."
            ),
            entryPage = "messages.html",
            requiredClues = listOf(
                ScamMsgClues.THREAD_OPENED_S1,
                ScamMsgClues.MESSAGE_DETAILS_S1
            ),
            lockedMessage = "Open the GCASH thread and the details of its last message first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "It proves the message is genuine — only GCash can post in that thread",
                    "It proves nothing. The sender ID is a label anyone can set, so a scam message lands in the same thread",
                    "It proves the phone has been hacked",
                    "It proves the SIM was cloned"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "Look at the route in the message details: the receipts and the last message " +
                        "did not arrive the same way.",
                "Ask what your phone actually checks before putting a message in a thread. The " +
                        "answer is: the name on it, and nothing else."
            ),
            successFeedback = "Nothing at all. The receipts came through the wallet's own " +
                    "messaging route; the last one was injected by a bulk sender with the label " +
                    "set to GCASH. Your phone can't tell them apart, so it stacks them together. " +
                    "Judge the request, never the thread.",
            failureFeedback = "Your phone was not compromised — look again at the message " +
                    "details and at what the phone actually verifies before grouping messages."
        ),

        LabTask(
            id = "t2",
            title = "A prize you never entered",
            objective = "Read the ₱850,000 raffle text. Identify the one feature that marks " +
                    "every prize scam, whatever the story around it.",
            guide = listOf(
                "Prize smishing is the oldest text scam in the country and it still works, " +
                        "because the message is engineered to make you feel lucky rather than " +
                        "suspicious. The amount is large but not absurd, the raffle is named " +
                        "after something real, and there is a reference number to make it feel " +
                        "administrative.",
                "Underneath, every version does the same thing: to release a prize you did not " +
                        "enter, you must first send money — a processing fee, a courier charge, " +
                        "DST or tax, a \"refundable\" deposit. Sometimes it is not money but " +
                        "your details, or a load transfer. Either way, value flows out of you " +
                        "before anything flows in.",
                "A genuine prize never requires a payment to receive it, and no legitimate " +
                        "raffle contacts winners only by text from a personal number. Notice " +
                        "also that this message uses your first name — since SIM registration, " +
                        "leaked lists mean scammers often know that much."
            ),
            steps = listOf(
                "Open the phone and tap the thread from +63 917 xxx xxxx about a raffle.",
                "Read the message, including the small print at the end.",
                "Ask three questions: did I enter this? who is it from? what do I have to do " +
                        "to collect?",
                "Do not reply, not even to ask. Replying in this lab costs a heart, and in " +
                        "real life it tells them a person is holding the phone.",
                "Swipe down and choose the feature that gives every prize scam away."
            ),
            entryPage = "thread.html#s2",
            requiredClues = listOf(ScamMsgClues.THREAD_OPENED_S2),
            lockedMessage = "Open the raffle thread first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "The message has typing errors",
                    "It comes from a mobile number instead of a company name",
                    "You have to send money or details first before you can receive the prize",
                    "The amount is very large"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Typing errors and odd numbers are common, but plenty of scams are written " +
                        "perfectly and come from a sender ID.",
                "Follow the money. Which direction does it move first?"
            ),
            successFeedback = "Paying to receive is the constant. Fees, taxes, courier charges " +
                    "and \"refundable\" deposits are all the same move, and it never comes with " +
                    "a real prize. Typos and odd numbers are hints; pay-to-receive is proof.",
            failureFeedback = "That's a hint, not proof — well-written scams from sender IDs " +
                    "exist too. Look at what you're asked to do before the prize arrives."
        ),

        LabTask(
            id = "t3",
            title = "The parcel you never ordered",
            objective = "The courier text says a package is held pending a ₱195 fee. Open its " +
                    "link in the sandbox, inspect the page, and submit the flag hidden in it.",
            guide = listOf(
                "Parcel smishing works on volume and timing. Enough people are waiting for a " +
                        "delivery at any moment that a message about a held package finds a " +
                        "real expectation to attach itself to, and ₱195 is small enough to pay " +
                        "without thinking.",
                "The page behind the link is a payment form. Unlike a password page, it asks " +
                        "for a card number, expiry and CVV, which is everything needed to charge " +
                        "the card repeatedly. Some versions ask you to \"verify\" with an OTP, " +
                        "which is the step that authorises a much larger transaction than the " +
                        "one displayed.",
                "Two checks defeat this. First, couriers in the Philippines do not collect " +
                        "customs or release fees by SMS link — fees are paid on delivery or " +
                        "through the official app. Second, check whether you are expecting " +
                        "anything at all, and track it in the app you installed yourself, not " +
                        "through a link someone texted you."
            ),
            steps = listOf(
                "Open the phone and tap the parcel thread.",
                "Tap the link. It opens a safe copy of the courier page in the sandbox.",
                "Look at the address panel above the page: who really owns that site?",
                "Do not type card details — that costs a heart here, and your card in real life.",
                "Tap INSPECT PAGE, find the CYBERITY{...} value in the code, and type it in " +
                        "below with the braces."
            ),
            entryPage = "thread.html#s3",
            requiredClues = listOf(
                ScamMsgClues.PARCEL_PAGE_OPENED,
                ScamMsgClues.PARCEL_PAGE_INSPECTED
            ),
            lockedMessage = "Open the courier link in the sandbox, then inspect the page code.",
            answer = LabAnswer.Flag(
                sha256 = "58df52ff6516c2dab39317d9aa5701127610515b44365a5540477d9034bab0b0"
            ),
            hints = listOf(
                "The INSPECT PAGE button is under the payment form.",
                "The flag is the value of the hidden field the form sends along with your card."
            ),
            successFeedback = "Captured. The ₱195 on screen is bait: the form collects the full " +
                    "card number, expiry and CVV, and the same kit is used against hundreds of " +
                    "numbers a day. Track parcels in the courier's own app, never through a " +
                    "texted link.",
            failureFeedback = "Not the flag. Open the page in the sandbox, tap INSPECT PAGE, " +
                    "and read the hidden field in the form."
        ),

        LabTask(
            id = "t4",
            title = "The bank on the phone",
            objective = "Play the recorded call from \"BPI Fraud Department\". Decide what " +
                    "single fact ends the call, no matter how convincing the caller is.",
            guide = listOf(
                "Vishing is the same attack delivered by voice, and voice is harder to resist " +
                        "than text. A calm, professional caller who already knows your name, " +
                        "your bank and the last four digits of your card feels like proof of " +
                        "legitimacy. It isn't: card databases leak constantly, and the last four " +
                        "digits are printed on every receipt.",
                "This call uses a clever inversion. The caller is not asking for your money — " +
                        "he is protecting you from a fraudulent transaction, and the OTP is " +
                        "needed to cancel it. Now you are on the same side, in a hurry, being " +
                        "helpful. The OTP he wants actually authorises his transaction.",
                "One fact settles it every time: no bank, wallet, telco or government office " +
                        "will ever ask for your OTP, PIN or password — not by call, text, chat, " +
                        "or email. There is no exception, no emergency, and no department for " +
                        "which it is different. Hang up, then call the number printed on your " +
                        "card or in the official app."
            ),
            steps = listOf(
                "Open the phone and tap RECENT CALLS, then play the recorded call.",
                "Listen for what the caller already knows, and when the tone changes to urgency.",
                "Notice exactly what he asks for, and the reason he gives for needing it.",
                "Play it to the end, so you hear how he handles hesitation.",
                "Swipe down and choose the fact that ends the call."
            ),
            entryPage = "call.html",
            requiredClues = listOf(ScamMsgClues.CALL_PLAYED),
            lockedMessage = "Play the recorded call to the end first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "He called from a mobile number, and banks only use landlines",
                    "He knew the last four digits of the card, which only the real bank could know",
                    "No bank ever asks for an OTP, for any reason — so the request itself is the proof",
                    "He refused to give his employee number"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Numbers can be spoofed and employee numbers invented, so neither settles " +
                        "anything.",
                "What did he ask you to read out, and who is that code actually for?"
            ),
            successFeedback = "The request itself is the proof. An OTP exists to prove that the " +
                    "account owner is authorising this transaction — so anyone asking for yours " +
                    "is trying to authorise theirs. Hang up and dial the number on your card; a " +
                    "genuine fraud desk will find you in their system.",
            failureFeedback = "Caller numbers are easy to spoof, and knowing the last four " +
                    "digits proves nothing — they're printed on receipts. Look at what he asked " +
                    "you to hand over."
        ),

        LabTask(
            id = "t5",
            title = "What to do with all of them",
            objective = "You have four scam messages and one scam call. Choose the response " +
                    "that actually reduces what happens next.",
            guide = listOf(
                "The instinct is to reply — to tell them off, to type STOP, or to ask \"who is " +
                        "this?\" All three do the same thing: they confirm that a real person " +
                        "reads messages on this number. Confirmed numbers are worth more and get " +
                        "sold on, which is why one reply is usually followed by more scams, not " +
                        "fewer.",
                "What helps is silence plus reporting. Don't reply, don't tap, then block the " +
                        "number and forward the message to your network's spam-report service " +
                        "so the sender can be shut down. Since the SIM Registration Act, " +
                        "reporting also gives the telco a registered identity to act against, " +
                        "which is why scammers increasingly use hijacked accounts instead.",
                "Two more habits are worth building. Warn the people who trust you, especially " +
                        "older relatives, because prize and bank calls are aimed at them hardest. " +
                        "And if you ever did enter details somewhere, act immediately: change the " +
                        "password, call the bank on its official number, and tell the ITSO if a " +
                        "campus account is involved. Speed limits the damage more than anything " +
                        "else."
            ),
            steps = listOf(
                "Go back to the message list and look at all four threads together.",
                "Ask what each one gains if you reply, even briefly.",
                "Think about what a telco can act on, and what it cannot.",
                "Swipe down and choose the response that leaves you with fewer of these, not " +
                        "more."
            ),
            requiredClues = listOf(
                ScamMsgClues.THREAD_OPENED_S1,
                ScamMsgClues.THREAD_OPENED_S2,
                ScamMsgClues.THREAD_OPENED_S3,
                ScamMsgClues.THREAD_OPENED_S4
            ),
            lockedMessage = "Read all four threads before deciding how to handle them.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Reply STOP to each one so they remove you from their list",
                    "Reply asking who they are, to find out who is behind it",
                    "Don't reply or tap anything. Block the sender, report it to your telco, and warn the people around you",
                    "Ignore them and do nothing else — they will stop eventually"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "A reply is information. What does it tell the sender?",
                "Doing nothing is safe for you but leaves the sender free to work through " +
                        "everyone else."
            ),
            successFeedback = "Silence, block, report, warn. Replying — even STOP — marks your " +
                    "number as live and reaches no list that anyone honours. Reporting is the " +
                    "only step that affects the sender rather than just you, and warning your " +
                    "family is what protects the people these are really aimed at.",
            failureFeedback = "Replying in any form tells them a real person is here. Look for " +
                    "the option that protects you and does something about the sender."
        )
    )
)
