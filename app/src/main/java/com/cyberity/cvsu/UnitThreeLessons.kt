package com.cyberity.cvsu

// ===========================================================================
// UNIT 03 LESSONS — Phishing & Social Engineering (levels 301-305)
// ===========================================================================
// Original teaching examples. Every name, domain and amount here is fictional
// and deliberately different from the lab's own evidence, so a lesson teaches
// the skill without handing over a task's answer. References support the
// concepts, not the invented incidents. Claim-to-source mapping:
// docs/lesson-source-review.md (Unit 03 section).

// ---------------------------------------------------------------------------
// LEVEL 301 — What is Phishing? (Phish Report Desk)
// ---------------------------------------------------------------------------
internal fun phishDeskLesson() = reviewedLesson(301, "What Phishing Really Wants", listOf(
    LessonPage("Genuine mail informs; phishing asks", LessonIcon.PHISHING,
        listOf("**Phishing** is social engineering delivered electronically: a message that pretends to come from someone you trust so that you will **do something** for the sender. Logos and names are easy to copy, so the most useful question is not *who* it says it is from but **what it wants from you**.",
            "A real notice from an office you deal with usually **informs** and leaves the next step to you. A phish **asks**: sign in here, open this, send that, and do it **now**. NIST lists **requests for sensitive information** and a **sense of urgency** among the cues that mark a phishing message."),
        listOf("mitre-phishing", "nist-phish-scale", "cisa-report-phishing"), task = 1,
        visual = lessonEvidence("TWO FICTIONAL NOTICES", listOf(
            "A: Library hours change on Monday. No action needed.",
            "B: Your borrowing account is locked. Sign in within 1 hour to restore it.",
            "Only one of them needs anything from you")).copy(highlights = listOf(1)),
        keyTerms = listOf(KeyTerm("Phishing", "A deceptive message that impersonates someone trusted to get you to act or hand over information."),
            KeyTerm("Cue", "A detail in a message that helps reveal it as phishing."))),
    LessonPage("How much homework did the attacker do?", LessonIcon.SEARCH,
        listOf("Some phishing is **mass** mail: one generic message sent to as many people as possible. **Spearphishing** is the opposite: a **specific person or group** is chosen first and the message is built around them.",
            "The more a message fits your real work, the harder it is to catch. NIST calls this **premise alignment**: a message that mimics a process you actually take part in, at a time it makes sense, scores as much harder to detect. Ask **who it was addressed to** and **how much it knew** about them."),
        listOf("mitre-phishing", "nist-phish-scale"), task = 2,
        keyTerms = listOf(KeyTerm("Spearphishing", "Phishing aimed at a chosen person, company or group, built from research about them."),
            KeyTerm("Premise alignment", "How closely a phish matches the target's real roles and routines; closer means harder to spot."))),
    LessonPage("A pass is not a person", LessonIcon.EMAIL,
        listOf("Mail systems check whether a message really came from the **domain** in its From line. That is useful, but it answers *where*, not *who*. If an attacker **steals the password** of a real account, the mail genuinely comes from that domain and every check passes.",
            "MITRE notes that phishing is sent from **compromised legitimate accounts**, and the FBI describes scammers taking over real email accounts to make requests. When a message from a real address asks for something out of character, the account itself may be the thing that was stolen."),
        listOf("mitre-phishing", "fbi-bec", "dmarc"), task = 3,
        visual = lessonEvidence("FICTIONAL GATEWAY SUMMARY", listOf(
            "From: a.reyes@school.example",
            "spf=pass  dkim=pass  dmarc=pass",
            "Asks: \"Sign in again to read my comments on your draft\"",
            "A real account can still be in the wrong hands")).copy(highlights = listOf(2)),
        keyTerms = listOf(KeyTerm("Compromised account", "A real account that an attacker controls, usually with a stolen password."))),
    LessonPage("The file is not what its name says", LessonIcon.CODE,
        listOf("A **spearphishing attachment** depends on you opening it. Attackers make the file look like a document by changing its **icon** and its **name**, for example by giving it **two extensions** so that only the harmless-looking one shows.",
            "Computers often **hide the last extension** of a file name, and phones cut long names short. Before opening an attachment, check the **full name and real type** of the file, and preview it somewhere it can't run. A \"document\" that turns out to be a program or a web page is the hook."),
        listOf("mitre-attachment", "mitre-double-ext"), task = 4,
        visual = lessonEvidence("FICTIONAL ATTACHMENT LIST", listOf(
            "Shown:  Enrolment_Summary.docx",
            "Actual: Enrolment_Summary.docx.exe",
            "The last extension decides what the file does")).copy(highlights = listOf(1)),
        keyTerms = listOf(KeyTerm("Extension", "The ending of a file name, like .pdf or .exe, that tells the computer how to open it."),
            KeyTerm("Double extension", "A name like report.pdf.exe, where the hidden last part is the real type."))),
    LessonPage("No link, no file, still phishing", LessonIcon.WARNING,
        listOf("Not every phish carries a payload. Some only start a **conversation**: an urgent request from someone senior, asking you to reply, pay or send information. With **nothing to scan**, filters have little to catch, and the whole attack rests on you acting.",
            "The defence is a **second channel**. Don't reply, and don't use the phone number or address in the message. **Contact the person yourself** through a number or website you already trust, and confirm before any money or information moves."),
        listOf("mitre-phish-info", "fbi-bec", "cisa-report-phishing"), task = 5,
        visual = LessonVisual.DoDont(
            dos = listOf("Confirm through a contact you looked up yourself", "Report the message to IT"),
            donts = listOf("Reply to ask if it's real", "Call the number in the signature", "Pay first and check later")))
), listOf(
    LessonQuestion("A message from a real classmate's address asks you to sign in on a new page to read her comments. Every authentication check passed. What does the pass prove?",
        listOf("That your classmate typed it herself", "That it came from her school's mail system, not who was using the account", "That the link is safe to open", "Nothing at all"), 1,
        "Checks confirm the sending domain. A stolen password lets an attacker send from inside the real account."),
    LessonQuestion("An attachment shows as Schedule.pdf on your laptop. What should you check before opening it?",
        listOf("Its full file name and real type", "Whether it has a nice icon", "Whether the email was polite"), 0,
        "The last extension may be hidden. A second, executable extension is a common disguise."),
    LessonQuestion("An urgent plain-text email from \"the Dean\" asks you to buy gift cards and reply with the codes. There is no link or attachment. What is the right move?",
        listOf("Reply asking whether it's really her", "Do it quickly, since there is nothing dangerous to click", "Forward it to classmates as a warning", "Contact the Dean's office on a number you looked up yourself"), 3,
        "With no payload, the request itself is the attack. Confirm through a channel the message didn't give you.")
))

// ---------------------------------------------------------------------------
// LEVEL 302 — Identifying Suspicious Emails (Bait Workshop)
// ---------------------------------------------------------------------------
internal fun baitWorkshopLesson() = reviewedLesson(302, "Reading an Email's Address and Links", listOf(
    LessonPage("The owner sits at the right-hand end", LessonIcon.LINK,
        listOf("In an email address, everything before the **@** is chosen by whoever runs the mailbox. The **domain** after it says where the mail is delivered. Web addresses work the same way: the **registered domain** just before the first single slash decides who controls the page.",
            "Owners can put **any words they like** in front of their own domain. Attackers use that to make a trusted name appear first, because people read **left to right** and stop at the first name they recognise. Read the address **from the right** instead, back from the first single slash."),
        listOf("rfc-address", "mitre-domains"), task = 1,
        visual = lessonEvidence("READ FROM THE RIGHT", listOf(
            "https://library.school.example.renew-books.example/login",
            "First single slash:   .../login",
            "Name just before it:  renew-books.example",
            "That owner chose 'library.school.example' too")).copy(highlights = listOf(2)),
        keyTerms = listOf(KeyTerm("Registered domain", "The name someone actually owns, such as renew-books.example. Everything to its left is chosen by that owner."))),
    LessonPage("Letters that only look the same", LessonIcon.SEARCH,
        listOf("A **lookalike** domain swaps one character for another that looks the same. Unicode calls these **confusables**: a Cyrillic letter can be pixel-for-pixel identical to a Latin one while being a completely different character, so the result is a different domain anyone can register.",
            "Browsers defend against the strongest version. Chrome shows a name in its raw **xn--** form (**punycode**) when its letters mix alphabets or imitate Latin letters from another script. Swaps that use only ordinary letters, such as **rn** for **m**, contain nothing unusual for that rule to catch, so you still have to read carefully."),
        listOf("unicode-confusables", "chromium-idn", "mitre-domains"), task = 2,
        visual = lessonEvidence("FICTIONAL LOOKALIKES OF campus.example", listOf(
            "carnpus.example     rn in place of m",
            "campus.exarnple     the swap can sit anywhere",
            "xn--...example      a non-Latin lookalike, shown raw")).copy(highlights = listOf(2)),
        keyTerms = listOf(KeyTerm("Homoglyph", "A character that looks like another, used to imitate a trusted name."),
            KeyTerm("Punycode", "The raw xn-- form of an international domain name, which reveals non-Latin characters."))),
    LessonPage("The text is not the destination", LessonIcon.LINK,
        listOf("A link is two things the sender types separately: the **text you see** and the **address it opens**. NIST lists **URL hyperlinking** among its phishing cues, because the text can show one site while the link goes to another.",
            "So the text is never evidence. **Preview the real destination** first: hold the link on a phone or rest the pointer on it on a laptop, then read who owns that address. A **QR code** hides the address completely until it's scanned, so check the address your scanner shows before you open it."),
        listOf("nist-phish-scale", "fbi-qr"), task = 3,
        visual = LessonVisual.Link(
            shownText = "https://portal.school.example",
            realDestination = "https://portal-school.verify-now.example/")),
    LessonPage("What DMARC actually checks", LessonIcon.SHIELD,
        listOf("The **From** line is typed by the sender, so receiving servers check it. **SPF** and **DKIM** each confirm a domain, and **DMARC** asks whether that domain **lines up with the domain in the From address**.",
            "Read the result carefully. A DMARC pass only means the **owner of that From domain** authorised the message. The standard itself says it does **not** deal with **visually similar domains**. An attacker who registers a lookalike can pass every check **for their own domain**."),
        listOf("dmarc"), task = 4,
        visual = lessonEvidence("FICTIONAL HEADER", listOf(
            "From: Records Office <records@school-records.example>",
            "Authentication-Results: spf=pass dkim=pass",
            "  dmarc=pass header.from=school-records.example",
            "Passed, for a domain the school may not own")).copy(highlights = listOf(2)),
        keyTerms = listOf(KeyTerm("DMARC", "A check that the domain in the From address authorised the message."),
            KeyTerm("Alignment", "Whether the domain SPF or DKIM verified matches the From domain."))),
    LessonPage("Count the tells, ignore the decoration", LessonIcon.WARNING,
        listOf("A well-made phish combines several tricks. NIST groups the cues into **technical** ones (sender address, lookalike domain, link destination), **presentation** ones (imitated logos) and **content** ones (urgency, threats, requests for information).",
            "Some details look official but prove nothing either way: a **logo**, a **footer address** or **your name in the greeting** cost an attacker nothing to copy. CISA adds that **spelling mistakes** are no longer a reliable sign, because AI can now write polished messages. Focus on what the message wants and where it really leads."),
        listOf("nist-phish-scale", "cisa-report-phishing"), task = 5,
        visual = LessonVisual.DoDont(
            dos = listOf("Check the sender's domain", "Preview where links go", "Notice deadlines and pressure"),
            donts = listOf("Trust a logo", "Trust your own name in the greeting", "Rely on spotting typos")))
), listOf(
    LessonQuestion("Who controls https://registrar.school.example.fee-portal.example/pay?",
        listOf("registrar.school.example", "Whoever registered fee-portal.example", "The school, because its name appears first", "Whoever owns the /pay page"), 1,
        "Read back from the first single slash. Everything to the left of the registered domain is chosen by its owner."),
    LessonQuestion("An email shows dmarc=pass. Which question does that NOT answer?",
        listOf("Whether its From domain is one you should trust", "Whether the From domain's owner authorised it", "Whether the checks lined up with the From domain"), 0,
        "DMARC validates the domain named in From. It doesn't judge whether that domain is the one you think it is."),
    LessonQuestion("Why is carnpus.example harder to catch than a lookalike that uses Cyrillic letters?",
        listOf("It is longer", "Browsers block every Cyrillic domain", "It uses only ordinary letters, so the browser shows it normally", "It can't receive email"), 2,
        "Browsers reveal many non-Latin lookalikes as xn-- punycode. Same-alphabet swaps need careful reading.")
))

// ---------------------------------------------------------------------------
// LEVEL 303 — Social Engineering (Campus Security Review)
// ---------------------------------------------------------------------------
internal fun socialEngineeringLesson() = reviewedLesson(303, "Attacks That Target People", listOf(
    LessonPage("The door you hold open", LessonIcon.LOCK,
        listOf("**Tailgating** is getting into a secure area by following someone who is allowed in. UK government security guidance lists the usual tactics: waiting to follow someone through a door, asking for help with a **heavy box**, or dressing as **maintenance or delivery** staff.",
            "Badges only work if each person uses their own. If someone without an ID or visitor pass is inside, the guidance is to **challenge them** if you feel comfortable, and otherwise **report them** through the proper channel."),
        listOf("uk-tailgating"), task = 1,
        visual = LessonVisual.DoDont(
            dos = listOf("Let each person badge in", "Direct visitors to reception or security", "Report someone who slipped in"),
            donts = listOf("Hold a badge door for a stranger", "Assume a uniform proves a role")),
        keyTerms = listOf(KeyTerm("Tailgating", "Following an authorised person through a controlled door. Also called piggybacking."))),
    LessonPage("A role and a reason", LessonIcon.WARNING,
        listOf("**Pretexting** means inventing a **role** that gives authority and a **reason** that gives urgency. MITRE describes attackers posing as a source with a legitimate reason to collect information, and directing victims to a **phone call** to get it.",
            "Caller ID and a confident voice prove nothing: the FCC warns that the number shown can be **spoofed**. When a caller asks for a password or a code, stop. **Hang up and call the office yourself** on a number from an official source."),
        listOf("mitre-phish-info", "fcc-spoofing"), task = 2,
        keyTerms = listOf(KeyTerm("Pretexting", "Inventing a believable role and reason to get someone to share information or access."),
            KeyTerm("Spoofing", "Faking the caller ID or sender details shown to the target."))),
    LessonPage("Curiosity is the payload", LessonIcon.CODE,
        listOf("**Baiting** leaves something tempting where the target will find it. In a 2016 study, researchers dropped **297 USB drives** on a university campus. People plugged them in at an estimated rate of **45–98%**, the first in **under six minutes**, often while trying to find the owner.",
            "MITRE describes malware copied to removable media, sometimes **renamed to look like a normal file**, that can run when the drive is inserted if **Autorun** is on. Organisations disable Autorun and limit removable media. For you, the safe habit is simple: **don't plug in a drive you found**. Hand it in."),
        listOf("usb-study", "mitre-removable"), task = 3,
        keyTerms = listOf(KeyTerm("Baiting", "Leaving a tempting item, like a labelled USB drive, for a victim to use."),
            KeyTerm("Autorun", "A feature that can start a program automatically when media is inserted."))),
    LessonPage("A familiar name asking for a code", LessonIcon.SHIELD,
        listOf("A message from a **friend's account** gets past the suspicion you'd apply to a stranger. MITRE notes that attackers take over real social media accounts because people **already trust** them, then use them to contact the owner's connections.",
            "A **one-time code** sent to your phone proves you control that number. GCash, for example, states it will **never ask for your MPIN or OTP**. If anyone asks for yours, even a friend, assume their account was taken over, don't send it, and **contact them another way**. Under Philippine law (RA 12010), tricking someone into giving up their financial account credentials, including by message, is a crime."),
        listOf("mitre-social-accounts", "gcash-safety", "ra-12010"), task = 4,
        visual = LessonVisual.DoDont(
            dos = listOf("Call or meet the friend to check", "Warn them their account may be hijacked"),
            donts = listOf("Send a code someone else asks for", "Trust the profile photo and name"))),
    LessonPage("One attack, several steps", LessonIcon.SEARCH,
        listOf("Real social engineering is often a **chain**. MITRE places **phishing for information** under reconnaissance: details collected early are used later to make the next approach convincing.",
            "So review incidents **together and in order**. A visitor who only wandered a corridor, a caller who knew a staff member's name, and a message that knew your schedule may be one person's campaign. Ask what each step **gained** and what it made possible next."),
        listOf("mitre-phish-info", "uk-tailgating"), task = 5,
        visual = lessonEvidence("FICTIONAL WEEK", listOf(
            "Mon  Visitor reads names on a staff noticeboard",
            "Tue  Caller uses one of those names to sound genuine",
            "Wed  Request for a login 'to fix a problem'",
            "Small gains add up to access")).copy(highlights = listOf(1)))
), listOf(
    LessonQuestion("Someone in a delivery uniform with full hands asks you to hold the badge door. What does the uniform prove?",
        listOf("That a delivery was booked", "That security already checked him", "Nothing; uniforms are an easy costume"), 2,
        "Tailgaters often pose as delivery or maintenance staff. Direct him to reception instead."),
    LessonQuestion("A friend's account messages you asking for the code just texted to your phone. What is most likely happening?",
        listOf("Someone is trying to get into your account using her hijacked one", "She locked herself out and needs help", "The code is public and expires anyway", "Your phone has a virus"), 0,
        "A code sent to you proves control of your number. Nobody else has an honest reason to need it."),
    LessonQuestion("In the USB study, why did many people plug in the drives?",
        listOf("They were told to", "Often to find the owner", "The drives looked expensive"), 1,
        "The researchers found people often seemed to be acting out of altruism, and appearance didn't matter much.")
))

// ---------------------------------------------------------------------------
// LEVEL 304 — Scam Messages (Your Phone, One Week)
// ---------------------------------------------------------------------------
internal fun scamMessagesLesson() = reviewedLesson(304, "Scams by Text and Call", listOf(
    LessonPage("A sender name is just a label", LessonIcon.PHISHING,
        listOf("Companies text you under a short name instead of a number. That name is a **sender ID**, and your phone groups messages with the same ID into **one thread**.",
            "Australia's communications regulator warns that scammers use **fake sender IDs**, and that their texts then **appear in the same thread as real messages** from the brand. Where a message lands proves nothing. Judge it by **what it asks** and **where its link goes**."),
        listOf("acma-sender-id"), task = 1,
        visual = lessonEvidence("FICTIONAL THREAD: WALLETPH", listOf(
            "You received ₱500 from J. Cruz. Ref 0412",
            "You paid ₱120 to Kape Corner. Ref 0418",
            "Your wallet is restricted. Verify at walletph-restore.example")).copy(highlights = listOf(2)),
        keyTerms = listOf(KeyTerm("Sender ID", "The name shown in place of a number on a text. It is set by whoever sends the message."),
            KeyTerm("Smishing", "Phishing by SMS text message."))),
    LessonPage("Value never flows out first", LessonIcon.WARNING,
        listOf("Prize texts change the story, the amount and the sponsor, but they share one rule-breaker. The US Federal Trade Commission is blunt: **real prizes are free**, and if you must **pay a fee** to receive one, it is a scam.",
            "Other signs follow the same pattern: a prize for something you **never entered**, pressure to **act now**, and requests for **account or card details** to \"release\" it. GCash also lists unexpected prizes and pressure to act quickly among its scam warning signs."),
        listOf("ftc-prizes", "gcash-safety"), task = 2,
        keyTerms = listOf(KeyTerm("Advance fee", "Money you're asked to pay first, to receive something that never comes."))),
    LessonPage("A small fee, a big form", LessonIcon.LINK,
        listOf("Delivery texts work because someone is always waiting for a parcel. The FTC lists messages with **links to make a payment** among common phishing lures, and says legitimate companies generally don't ask you to update payment details through a link in a text.",
            "If you're curious, inspect the page **safely** rather than filling it in. Look at **who owns the address** and **what it collects**. A small fee with a full card form, or a request for an OTP, is aimed at much more than the fee."),
        listOf("ftc-phishing", "bpi-scams"), task = 3,
        visual = LessonVisual.Link(
            shownText = "Track your parcel: courier.ph/track/88213",
            realDestination = "https://courier-ph.release-fee.example/pay")),
    LessonPage("The one request no bank makes", LessonIcon.LOCK,
        listOf("**Vishing** is phishing by voice. Callers can sound well informed, and the FCC warns that the number on your screen can be **spoofed** to look like a bank or agency.",
            "One fact cuts through every story: BPI states that banks **don't ask for OTPs or passwords over the phone** and don't verify accounts through **unsolicited calls**. An OTP authorises a transaction, so a caller who wants yours wants to make one. **Hang up** and call the number printed on your card or the bank's official site."),
        listOf("fcc-spoofing", "bpi-scams"), task = 4,
        keyTerms = listOf(KeyTerm("Vishing", "Phishing carried out by phone call."),
            KeyTerm("OTP", "A one-time PIN that approves a single sign-in or transaction."))),
    LessonPage("What actually helps", LessonIcon.REPORT,
        listOf("Replying, even to say STOP or \"who is this?\", doesn't help. CISA's advice is to **report** a phishing message, **delete** it, and **not reply or click anything**, including unsubscribe links.",
            "In the Philippines, the government's **Inter-Agency Response Center** takes scam reports on hotline **1326**, run by the **CICC**. Report through your phone's spam option or the hotline, block the sender, and warn people who might get the same message."),
        listOf("cisa-report-phishing", "iarc-1326"), task = 5,
        visual = LessonVisual.DoDont(
            dos = listOf("Report and block", "Call 1326 to report a scam", "Warn family and friends"),
            donts = listOf("Reply STOP", "Ask who they are", "Tap the link to check")))
), listOf(
    LessonQuestion("A text appears under your wallet's name, right below your real receipts. What does that placement prove?",
        listOf("It's genuine, since only the wallet can post there", "Nothing; scammers can set the same sender ID", "Your phone was hacked"), 1,
        "Phones group texts by sender ID, which scammers can fake."),
    LessonQuestion("Which detail alone tells you a prize message is a scam?",
        listOf("It contains a typo", "The prize is large", "It comes from a mobile number", "You must pay before you can receive it"), 3,
        "Real prizes are free. A fee to collect is the scam, whatever the story."),
    LessonQuestion("A caller says he's from your bank's fraud team and needs your OTP to stop a charge. What do you do?",
        listOf("Hang up and call the number on your card", "Give it, since he's stopping fraud", "Ask for his employee number first"), 0,
        "Banks don't ask for OTPs by phone. Call back on a number you know is real.")
))

// ---------------------------------------------------------------------------
// LEVEL 305 — Phishing Simulation capstone (One Day, One Phone)
// ---------------------------------------------------------------------------
internal fun oneDayPhoneLesson() = reviewedLesson(305, "Checking Before You Trust", listOf(
    LessonPage("Check through a door you already have", LessonIcon.SHIELD,
        listOf("A sender name like an app, an office or a teacher is typed by whoever sent the message, so it settles nothing. What settles it is **checking through a channel the message didn't give you**.",
            "CISA's rule: if a message might be real, **don't use its links or phone numbers**. Go to the service yourself through a website, app or contact you **already trust**. If the same notice is waiting there, it's real."),
        listOf("cisa-report-phishing", "ftc-phishing"), task = 1,
        visual = LessonVisual.DoDont(
            dos = listOf("Open the app from your own home screen", "Type the site's address yourself"),
            donts = listOf("Tap the link to 'check'", "Trust the sender name"))),
    LessonPage("A Page's history tells on it", LessonIcon.SEARCH,
        listOf("Anyone can create a Facebook Page with an office's name and logo. Facebook's **Page transparency** information shows the **date a Page was created**, its **past names**, and the **ads it is running**.",
            "A real office usually has a long, steady history. A Page created recently, or one that used to be called something unrelated, is a warning. Find the official Page **through the institution's own website**, then **report** the impostor."),
        listOf("meta-transparency", "cisa-report-phishing"), task = 2,
        visual = lessonEvidence("FICTIONAL PAGE TRANSPARENCY", listOf(
            "Page name: University Grants Office",
            "Created: 3 days ago",
            "Previous name: Funny Cat Videos PH",
            "Ads running: 4")).copy(highlights = listOf(2))),
    LessonPage("A QR code is a hidden link", LessonIcon.LINK,
        listOf("The FBI warns that criminals swap or stick **malicious QR codes** over real ones, sending people to fake sites that collect **logins and payment details**.",
            "Treat a QR code like any link: after scanning, **read the address** before opening it and ask who owns it. Don't install apps from a QR code; use the official app store. Not every phishing page wants a password. A form that only asks for your **name, number and course** is still collecting material for the next scam."),
        listOf("fbi-qr"), task = 3,
        keyTerms = listOf(KeyTerm("Quishing", "Phishing that hides its link inside a QR code."))),
    LessonPage("Knowing your details isn't proof", LessonIcon.WARNING,
        listOf("A caller who knows your **name, course or number** sounds legitimate. But MITRE describes **phishing for information** as a way to collect details that are **used later** during targeting. Knowing about you shows someone **found** your details, not who they work for.",
            "The FCC's advice for any caller who claims to represent a company: **hang up**, and call back on a number from your **account statement** or the company's **official website**."),
        listOf("mitre-phish-info", "fcc-spoofing"), task = 4,
        visual = LessonVisual.DoDont(
            dos = listOf("Hang up and call the company yourself"),
            donts = listOf("Install an app a caller asks for", "Read out a code from your phone"))),
    LessonPage("Every domain has a birth record", LessonIcon.SEARCH,
        listOf("Domain registries publish **registration data**. A registration lookup (RDAP, or the older WHOIS) can show **when** a domain was registered, its **registrar**, and contacts in roles such as **registrant**, though privacy rules often **redact** personal details.",
            "MITRE notes that this data can be pivoted on: domains that share registration details can reveal **more of an attacker's infrastructure**. A real office's domain is usually years old; a phishing domain is often very new."),
        listOf("rdap", "mitre-domains"), task = 5,
        visual = lessonEvidence("FICTIONAL LOOKUP", listOf(
            "Domain:      grants-portal.example",
            "Registered:  2 days ago",
            "Registrar:   Example Registrar Inc.",
            "Registrant:  REDACTED FOR PRIVACY")).copy(highlights = listOf(1)),
        keyTerms = listOf(KeyTerm("RDAP / WHOIS", "Public lookups of who registered a domain, when, and through which registrar."))),
    LessonPage("Several lures, one attacker", LessonIcon.REPORT,
        listOf("A phishing campaign often uses **several channels at once**. MITRE's phishing techniques include email, messages through other services, and **voice calls**. Stories and channels are cheap to change.",
            "Group lures by what an attacker **can't change easily**: the **domains** they registered, **what they want** from you, and whether one message **already knew** what another collected. Linking two messages to one attacker is how a help desk stops the third."),
        listOf("mitre-phishing", "mitre-domains"), task = 6)
), listOf(
    LessonQuestion("A notice says it's from your class app. What proves it's genuine?",
        listOf("Its sender name", "The same notice inside the app, opened yourself", "It mentions your real teacher"), 1,
        "Names and details can be copied. Checking through your own route settles it."),
    LessonQuestion("A caller knows your full name, course and section. What does that prove?",
        listOf("He works where he says", "Your SIM was cloned", "He has your details, not that he is who he claims", "The offer is real"), 2,
        "Details leak and are collected on purpose. Hang up and call the company yourself."),
    LessonQuestion("Two lookalike domains share the same registrant contact. What does that suggest?",
        listOf("The same person or group may be behind both", "Both are official", "Registration data is always fake"), 0,
        "Shared registration details are a common way to link infrastructure to one attacker.")
))
