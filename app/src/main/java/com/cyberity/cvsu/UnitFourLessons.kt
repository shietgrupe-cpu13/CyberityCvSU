package com.cyberity.cvsu

// ===========================================================================
// UNIT 04 LESSONS — Malware (levels 401-405)
// ===========================================================================
// Original teaching examples. Every file name, machine, site and number here is
// fictional and deliberately different from the lab's own evidence, so a
// lesson teaches the skill without handing over a task's answer. References
// support the concepts, not the invented incidents (WannaCry in 405 is the one
// real event, cited as history). Claim-to-source mapping:
// docs/lesson-source-review.md (Unit 04 section).
//
// Level 450 (Bonus XP Cache) is a reward node with nothing to teach, so it has
// no lesson.

// ---------------------------------------------------------------------------
// LEVEL 401 — What is Malware? (Quarantine Vault)
// ---------------------------------------------------------------------------
internal fun whatIsMalwareLesson() = reviewedLesson(401, "Malware Is Named by What It Does", listOf(
    LessonPage("One word, many families", LessonIcon.SHIELD,
        listOf("**Malware** is short for **malicious software**. NIST's definitions put many kinds under that one word: **viruses, worms, trojan horses, spyware** and some **adware**. Each family is named for **what it does**: show ads, watch what you type, borrow your processor, take orders, lock your files.",
            "This lesson covers the families the rest of the unit doesn't (spyware, cryptominers, bots and adware), plus the question that decides whether something is malware at all. Viruses and trojans come in 402, ransomware in 403, prevention in 404, and 405 puts them all on one machine."),
        listOf("nist-malware-glossary"),
        keyTerms = listOf(KeyTerm("Malware family", "A group of malware named for a shared behaviour, such as spyware or ransomware."))),
    LessonPage("Same power, different intent", LessonIcon.SHIELD,
        listOf("NIST defines **malware** as software inserted into a system, **usually covertly**, to harm the **confidentiality, integrity or availability** of its data or the system itself. Notice what decides it: not what a program **can** do, but **whose interests it serves** and whether the owner **knows**.",
            "Two programs can hold exactly the same powers. One is installed by the device's owner and announced to its users; the other hides and works for a stranger. Antivirus can't tell the difference by pattern alone: NIST notes that it **misses unknown threats** and can **mislabel harmless activity** as malicious. A flag is a reason to look closer, **not a verdict**."),
        listOf("nist-malware-glossary", "nist-malware-guide"), task = 1,
        visual = lessonEvidence("TWO FICTIONAL PROGRAMS, ONE POWER", listOf(
            "ExamGuard  · records the screen during online exams",
            "             installed by the school, named in the syllabus",
            "WallpaperHD · records the screen whenever the phone is unlocked",
            "             installed from a forum link, hides its icon",
            "Same ability. Only one is malware.")).copy(highlights = listOf(3)),
        keyTerms = listOf(KeyTerm("Malware", "Software that works against the owner of a device, usually installed and run without their informed consent."),
            KeyTerm("False positive", "A harmless program that a security tool wrongly flags as malicious."))),
    LessonPage("The app that works perfectly", LessonIcon.LOCK,
        listOf("**Spyware** quietly collects information and sends it to someone else. A **keylogger** is spyware that records what you **type**, which makes it the fastest route to **passwords and PINs**. Because every extra day means more stolen data, spyware avoids anything you would notice, and is often wrapped inside an app that **genuinely works**.",
            "On Android the giveaway is usually a **permission the app has no reason to need**. MITRE describes malicious apps registering an **accessibility service** to read the text you type in **other apps**, or posing as a **third-party keyboard**. Google Play allows accessibility access mainly for tools that help **people with disabilities**, and requires other apps to **disclose plainly** what they collect with it."),
        listOf("mitre-mobile-keylog", "play-accessibility"), task = 2,
        visual = lessonEvidence("FICTIONAL APP: PocketTorch · flashlight", listOf(
            "Camera (flash)            expected",
            "Notifications             fine",
            "Accessibility service     reads every screen and keystroke",
            "Ask: does a flashlight need this?")).copy(highlights = listOf(2)),
        keyTerms = listOf(KeyTerm("Spyware", "Malware that secretly gathers information about you and sends it to someone else."),
            KeyTerm("Keylogger", "Spyware that records keystrokes, usually to steal logins and PINs."))),
    LessonPage("Stealing the processor", LessonIcon.WARNING,
        listOf("Some malware doesn't want your data at all. MITRE calls it **resource hijacking**: using a victim's computer for heavy work that pays someone else, most often **mining cryptocurrency**. The attacker collects the coins; the owner pays in **electricity, heat and battery wear**.",
            "MITRE's detection notes describe the signature: **high CPU use** that doesn't go away, from a process you didn't expect, plus an outbound connection to a **mining pool**. Many miners work hardest when you aren't looking, so check **when** the load happens, not only how big it is."),
        listOf("mitre-resource-hijack"), task = 3,
        visual = lessonEvidence("FICTIONAL LAPTOP MONITOR · 01:30", listOf(
            "Lid closed, no user signed in",
            "StickyNotesPlus.exe   CPU 81%   fan at maximum",
            "Connected to  pool.coin-share.example:4444",
            "Files opened: none   Keystrokes read: none")).copy(highlights = listOf(1, 2)),
        keyTerms = listOf(KeyTerm("Cryptojacking", "Secretly using someone else's device to mine cryptocurrency."),
            KeyTerm("Mining pool", "A service that pays miners for computing work, which is why a miner keeps connecting to one."))),
    LessonPage("A worker waiting for orders", LessonIcon.CODE,
        listOf("MITRE describes a **botnet** as a network of compromised systems that one operator can **direct together**. Each infected device is a **bot**. Between orders it does almost nothing, so its owner notices nothing. NIST calls the hidden control channel a **backdoor**: something that **listens for commands** and carries them out.",
            "When the order comes, every bot acts at once: mass **phishing**, or a **distributed denial-of-service (DDoS)** attack, where traffic from many machines floods a service until real users can't reach it. The victim is often **someone else**, and the address in their logs is **yours**. Read the orders to learn **what** was attacked, **when**, and from where."),
        listOf("mitre-botnet", "mitre-network-dos", "nist-malware-guide"), task = 4,
        visual = lessonEvidence("FICTIONAL ORDER LOG · bot on a café PC", listOf(
            "03-02 22:00  sleep 3600",
            "03-03 09:00  spam   list=buyers.txt",
            "03-04 18:30  flood  tickets.concert.example:443  600s",
            "Each line is an order. The bot just obeys.")).copy(highlights = listOf(2)),
        keyTerms = listOf(KeyTerm("Command-and-control (C2)", "The server or channel an attacker uses to send orders to infected devices."),
            KeyTerm("DDoS", "Distributed denial of service: many machines flood a target so real users can't get in."))),
    LessonPage("Ads, and naming by main purpose", LessonIcon.SEARCH,
        listOf("**Adware** earns money by forcing ads on you. The FTC lists the signs: **pop-up ads** everywhere, a **changed browser homepage**, **new toolbars** you didn't add, and being **redirected** to sites you didn't mean to visit. Malware often arrives with **free movies, music or games** from unfamiliar sites. NIST counts **spyware and some adware** as malicious code.",
            "Real samples mix behaviours, so analysts name each one by its **main purpose**: what does it **mostly do**, and **who profits**? Ads for someone else is adware; secret collection is spyware; your processor earning coins is a miner; waiting for remote orders is a bot. Software the owner **installed and announced** is not malware at all."),
        listOf("ftc-malware", "nist-malware-glossary"), task = 5,
        visual = LessonVisual.DoDont(
            dos = listOf("Ask what it mostly does", "Ask who profits", "Check who installed it"),
            donts = listOf("Trust the file name or icon", "Treat an antivirus flag as the final answer")))
), listOf(
    LessonQuestion("A school installs screen-monitoring software on its own lab PCs and tells every student in the lab rules. The same ability hidden in a game would be spyware. What makes the difference?",
        listOf("How powerful the software is", "Who installed it and whether users were told", "Whether antivirus flags it"), 1,
        "Malware is defined by intent and consent. Capability alone decides nothing."),
    LessonQuestion("A wallpaper app asks for accessibility access. Why is that a warning sign?",
        listOf("Accessibility access makes apps run slowly", "Wallpaper apps are always malware", "It is only allowed on paid apps", "It lets an app read what's on screen and what you type in other apps"), 3,
        "Accessibility access is powerful. An app that has no reason to need it may be logging your keystrokes."),
    LessonQuestion("A PC's processor sits at 90% at 2 AM, and the only odd connection goes to a mining pool. No files were read. What is being stolen?",
        listOf("Computing power and electricity", "Passwords", "Photos and documents"), 0,
        "A cryptominer uses your processor to earn coins for someone else. You pay for the power and the wear.")
))

// ---------------------------------------------------------------------------
// LEVEL 402 — Viruses & Trojans (Org Flash Drive)
// ---------------------------------------------------------------------------
internal fun virusesTrojansLesson() = reviewedLesson(402, "How Viruses and Trojans Get In", listOf(
    LessonPage("The two oldest ways in", LessonIcon.CODE,
        listOf("**Viruses** and **trojans** both need a **person** to let them in. NIST explains the difference: a virus **copies itself into files** that people share and open; a trojan **doesn't copy itself**, it looks like something you want, so you **run it yourself**.",
            "This lesson covers how a macro virus spreads and how its parts work, the warning signs in a trojan installer, checking a download's **hash**, and why cleanup must reach **every computer** that ran the virus."),
        listOf("nist-malware-guide"),
        keyTerms = listOf(KeyTerm("Virus", "Malware that copies itself into other files and spreads when people open them."),
            KeyTerm("Trojan horse", "Malware disguised as something useful, which relies on you running it."))),
    LessonPage("A virus needs a host and a person", LessonIcon.CODE,
        listOf("NIST defines a **virus** as code that **copies itself into host programs or data files**, and notes it is **usually triggered by a person** opening the file or running the program. It can't travel alone; it rides inside something people share.",
            "A **macro virus** uses the small programs (**macros**) that Office documents can carry, and NIST notes that it infects **documents and document templates**. Once a template is infected, documents made from it carry the virus too. Microsoft now **blocks macros in files from the internet** by default, because macros are a common way to deliver malware. To trace an outbreak, put events **in time order** and find the **first infected file**: it shows how the virus got in."),
        listOf("nist-malware-guide", "ms-macros"), task = 1,
        visual = lessonEvidence("FICTIONAL SHARED FOLDER · REVIEW CLUB", listOf(
            "Mon 10:02  Quiz_Bank_Free.docm   opened, macros enabled",
            "Mon 10:15  Reviewer_Ch1.docx     saved  +41 KB",
            "Tue 14:40  Reviewer_Ch2.docx     saved  +41 KB",
            "Same growth each time. Start at the earliest entry.")).copy(highlights = listOf(0)),
        keyTerms = listOf(KeyTerm("Host", "The file or program a virus attaches itself to."),
            KeyTerm("Macro", "A small program stored inside a document, run by the application that opens it."))),
    LessonPage("Parts with one job each", LessonIcon.SEARCH,
        listOf("Malicious code is easier to read when you split it by **job**. **Replication** makes the copies. A **trigger** waits for a condition, such as a **date** or a count; NIST's term for code that fires when set conditions are met is a **logic bomb**. The **payload** is the harm done when the trigger fires.",
            "Some lines exist only so **you won't notice**: hiding warnings, or letting your work save normally. Office asks before a document's macros may run; once you allow them, the code can switch off further alerts so the rest of its work goes unseen. Until its trigger fires, a virus stays **dormant** and keeps spreading quietly."),
        listOf("nist-malware-guide", "nist-logic-bomb", "ms-macros"), task = 2,
        visual = LessonVisual.Source(listOf(
            "' Fictional teaching example, not working code",
            "CopyInto ActiveTemplate          ' replication",
            "If Day(Now) = 28 Then            ' trigger",
            "    ClearRecentFiles             ' payload",
            "DisplayAlerts = False            ' camouflage")),
        keyTerms = listOf(KeyTerm("Trigger", "The condition that decides when malicious code acts."),
            KeyTerm("Payload", "The damage malicious code does once it acts."))),
    LessonPage("A gift you install yourself", LessonIcon.WARNING,
        listOf("NIST describes a **trojan horse** as a program that **looks harmless but has a hidden malicious purpose**, and which **does not copy itself**. It needs **you to run it**, which is why it is wrapped in something you want. The FTC warns that **free music, movies and games from unfamiliar sites** are a common way malware arrives, and \"free full version\" software is the same bait.",
            "Read what Windows tells you. A **User Account Control** prompt is **grey** for a program signed by a verified publisher and **yellow** for one that is **unsigned or untrusted**. Watch for **extra programs ticked for you**, especially anything that **starts with Windows**, and for any request to **turn off antivirus**: NIST's advice to users is never to disable it."),
        listOf("nist-malware-guide", "ftc-malware", "ms-uac", "mitre-user-exec"), task = 3,
        visual = LessonVisual.DoDont(
            dos = listOf("Download from the vendor's own site", "Read the publisher on the permission prompt", "Untick extras you didn't ask for"),
            donts = listOf("Run \"cracked\" or \"activated\" installers", "Turn off antivirus because an installer says so"))),
    LessonPage("The name can be copied, the hash can't", LessonIcon.LOCK,
        listOf("A file's **name** is just a label: renaming a file doesn't change its contents. A **hash** is computed from the contents themselves. Microsoft's documentation explains that changing **even a single character** of a file changes its hash, and that vendors publish the **SHA-256** of each download so you can compare.",
            "A SHA-256 hash is **256 bits**, written as **64 hexadecimal characters**. **All 64 must match.** Looking only at the first and last few is a trap: an attacker can keep tweaking a file until its ends happen to line up, but matching every character is practically impossible. Compare the whole string, or let a tool compare it for you."),
        listOf("ms-filehash"), task = 4,
        visual = lessonEvidence("FICTIONAL HASH CHECK · NoteMaker_Setup", listOf(
            "Published:  7c2e91…(54 more)…a03f",
            "Download A: 7c2e91…(54 more)…a03f   all 64 match",
            "Download B: 7c2e91…(54 differ)…a03f   ends only",
            "Same name. Only A is the vendor's file.")).copy(highlights = listOf(2)),
        keyTerms = listOf(KeyTerm("Hash", "A fixed-length fingerprint calculated from a file's contents. Any change gives a different hash."),
            KeyTerm("SHA-256", "A hash function whose result is 256 bits, shown as 64 hexadecimal characters."))),
    LessonPage("Human speed, and cleaning every host", LessonIcon.REPORT,
        listOf("Viruses travel at **human speed**: on flash drives, in attachments, through shared folders, and only where someone **opens** the infected file. NIST contrasts this with a **worm**, which spreads **without a person** and doesn't need a host file at all. You'll meet one in level 405.",
            "A macro virus runs only where an infected document is **opened with macros allowed**. A computer that merely **carried** the file, or opened a picture from the same drive, ran nothing. Cleanup must still reach **every** computer that did run it, because one missed template keeps infecting new documents."),
        listOf("nist-malware-guide", "ms-macros"), task = 5,
        visual = lessonEvidence("FICTIONAL CHECKLIST · who ran the macro?", listOf(
            "PC-A   opened Reviewer_Ch1.docx, macros on   → clean template",
            "PC-B   copied the file, never opened it      → no",
            "PC-C   opened a .jpg from the same drive     → no",
            "PC-D   opened Reviewer_Ch2.docx, macros on   → clean template")).copy(highlights = listOf(0, 3)))
), listOf(
    LessonQuestion("What does a virus need that a worm doesn't?",
        listOf("An internet connection", "Administrator rights", "A host file, and a person to open it"), 2,
        "NIST: a virus copies itself into host files and is usually triggered by a person. A worm spreads on its own."),
    LessonQuestion("Two downloads share a name, and both hashes start and end with the same six characters as the published one. What should you do?",
        listOf("Install either one", "Compare all 64 characters; only an exact match is the vendor's file", "Pick the smaller file"), 1,
        "Matching ends are cheap to fake. Every character has to match."),
    LessonQuestion("A \"free full version\" installer works fine but adds a service that starts with Windows. What is that pattern?",
        listOf("A trojan: a useful-looking program hiding a malicious part", "A normal installer", "A virus copying itself into files", "Adware only, so it's harmless"), 0,
        "A trojan relies on you running it, and a working program is the disguise.")
))

// ---------------------------------------------------------------------------
// LEVEL 403 — Ransomware (Ransomware at 2 AM)
// ---------------------------------------------------------------------------
internal fun ransomwareLesson() = reviewedLesson(403, "Ransomware, From Alarm to Recovery", listOf(
    LessonPage("Locked files, two ways back", LessonIcon.LOCK,
        listOf("**Ransomware** encrypts files and demands payment for the key. Without the key the files can't be read, which leaves two ways back: pay, or restore from a **backup the attackers couldn't reach**. The FBI advises against paying, and CISA notes that criminals now often **steal data too**.",
            "This lesson follows an incident in order: **contain** it, understand **what the attackers want**, find **which backups survived**, restore from the **right point in time**, and plan so the next one ends with a restore instead of a ransom."),
        listOf("cisa-ransomware-guide", "fbi-ransomware-psa"),
        keyTerms = listOf(KeyTerm("Ransomware", "Malware that encrypts files and demands payment for the key."),
            KeyTerm("Encryption", "Scrambling data so that it can only be read with the right key."))),
    LessonPage("Isolate first, don't switch off", LessonIcon.WARNING,
        listOf("Ransomware encrypts every file it can reach, including **shared folders** on the network, so one infected PC can lock a whole office's files. The #StopRansomware Guide from CISA, the FBI and NSA puts one step first: work out **which systems are affected** and **isolate them immediately**, by unplugging the cable or turning off Wi-Fi.",
            "The guide says to **power a device down only if you can't disconnect it**, because shutting down loses what is in **memory**: evidence of how the attack worked. It also advises capturing a **system image and memory** from affected machines. Cut the right machine off the network, and leave it running."),
        listOf("cisa-ransomware-guide"), task = 1,
        visual = LessonVisual.DoDont(
            dos = listOf("Find the machine writing the encrypted files", "Unplug its network cable or Wi-Fi", "Leave it powered on for investigators"),
            donts = listOf("Switch it off by reflex", "Only cut the internet and assume it's contained"))),
    LessonPage("Two threats, not one", LessonIcon.LOCK,
        listOf("CISA describes **double extortion**: attackers **steal a copy** of the data before encrypting it, then threaten to **publish** it. A good backup brings your files back, but it **can't un-steal** the copy. The deadlines and discounts on a ransom note are **pressure**, there to make you decide before you think.",
            "The FBI advises against paying: payment **doesn't guarantee** you get your data back and it **encourages more attacks**. Stolen personal records also make this a **personal data breach**. In the Philippines, NPC Circular 16-03 requires notifying the **National Privacy Commission** and affected people within **72 hours** of knowing about a breach."),
        listOf("cisa-ransomware-guide", "fbi-ransomware-psa", "npc-breach"), task = 2,
        visual = lessonEvidence("FICTIONAL RANSOM NOTE · excerpt", listOf(
            "Your files are encrypted.",
            "We also copied 6 GB from your HR folder.",
            "Pay in 48 hours or we publish it. 30% off today.",
            "Encryption is one threat. The copy is the second.")).copy(highlights = listOf(1)),
        keyTerms = listOf(KeyTerm("Double extortion", "Encrypting data and also threatening to leak a stolen copy."),
            KeyTerm("Personal data breach", "Personal information exposed or taken without authority, which must be reported."))),
    LessonPage("Which backups the malware could reach", LessonIcon.SHIELD,
        listOf("CISA warns that many ransomware variants **look for backups and delete or encrypt them**. The UK's NCSC has seen ransomware encrypt **USB and network drives left connected** as backups. If the infected PC could write to it, assume it is gone.",
            "A folder that **syncs** copies every change, and encryption is a change, so it isn't a backup unless older **versions** are kept. The FBI calls backups the most important defence against ransomware and says they should **not be connected** to the computers they protect. The NCSC adds that cloud backups can be protected by allowing **new data in but no overwriting**, and by guarding the accounts that manage them."),
        listOf("cisa-ransomware-guide", "ncsc-offline-backups", "fbi-ransomware-psa"), task = 3,
        visual = lessonEvidence("FICTIONAL BACKUP AUDIT · after the attack", listOf(
            "External disk, plugged in 24/7      encrypted",
            "Synced cloud folder                 holds encrypted copies",
            "Disk in a locked drawer             readable",
            "Ask of each: could the infected PC change it?")).copy(highlights = listOf(2))),
    LessonPage("Restore from before the break-in", LessonIcon.SEARCH,
        listOf("Encryption is the **end** of the attack. The #StopRansomware Guide calls ransomware deployment **the last step** in a network compromise, and warns that an infection may be evidence of an **earlier, unresolved** one. Attackers often get in days before, look around, steal data and plant a way back in.",
            "Any backup made **after** they got in may contain **what they left behind**. The guide says to take care **not to re-infect** clean systems during recovery. So restore from **before the break-in**, not merely from before the encryption, and scan the restore first. Losing a few more days of edits is recoverable; letting the attacker back in isn't."),
        listOf("cisa-ransomware-guide"), task = 4,
        visual = lessonEvidence("FICTIONAL TIMELINE", listOf(
            "Fri 16:20  phishing login, attacker gets in",
            "Fri 16:45  remote-access tool installed",
            "Tue 03:10  encryption starts",
            "A Saturday snapshot brings the tool back too.")).copy(highlights = listOf(1))),
    LessonPage("Plan for the day it happens", LessonIcon.REPORT,
        listOf("The NCSC describes the long-standing **3-2-1** habit: at least **3** copies, on **2** different devices, with **1** off-site. It protects against a dead disk or a fire. But the NCSC points out that 3-2-1 **doesn't require any copy to be offline**, so ransomware needs one more rule: keep **at least one backup offline** or otherwise **out of reach** of the network.",
            "Then **test** it. CISA advises keeping offline, encrypted backups and **regularly testing** that they can be restored. A backup that has never been restored is a hope, not a plan."),
        listOf("ncsc-offline-backups", "cisa-ransomware-guide"), task = 5,
        visual = LessonVisual.DoDont(
            dos = listOf("Keep 3 copies on 2 kinds of storage, 1 off-site", "Keep 1 offline or unchangeable from the network", "Practise a restore on a schedule"),
            donts = listOf("Count a sync folder as a backup", "Leave the backup disk plugged in")))
), listOf(
    LessonQuestion("Files on a shared drive are being encrypted right now. What does the #StopRansomware Guide put first?",
        listOf("Pay before the price rises", "Switch every PC off", "Delete the ransom note", "Isolate the affected machine from the network"), 3,
        "Disconnecting stops the spread. Powering down is a last resort because it loses evidence in memory."),
    LessonQuestion("An office restored everything from a clean offline backup. Why might it still have a problem?",
        listOf("Offline backups can't be restored", "The attackers may have stolen a copy and can still publish it", "Restored files are always corrupted"), 1,
        "That's double extortion. A backup fixes the encryption, not the theft."),
    LessonQuestion("Which copy is most likely to survive ransomware?",
        listOf("A disk stored disconnected from the network", "A USB drive left plugged in", "A cloud folder that syncs every change"), 0,
        "Anything the infected machine can write to can be encrypted along with everything else.")
))

// ---------------------------------------------------------------------------
// LEVEL 404 — Malware Prevention (Lab 3 Hardening Sprint)
// ---------------------------------------------------------------------------
internal fun malwarePreventionLesson() = reviewedLesson(404, "Keeping Malware Out", listOf(
    LessonPage("Cheaper than cleanup", LessonIcon.SHIELD,
        listOf("Everything so far in this unit happened **after** malware got in. NIST's malware guide puts prevention first: **patching**, **least privilege**, **application allowlisting** and **user awareness**, each closing a different way in.",
            "This lesson covers which updates to install **first**, how an allowlist works and where it fails, what a **standard account** changes, **hardening** that people can still work with, and why you need **layers** rather than one product."),
        listOf("nist-malware-guide"),
        keyTerms = listOf(KeyTerm("Attack surface", "All the ways an attacker could get into a system. Prevention makes it smaller."))),
    LessonPage("Patch what attackers are using today", LessonIcon.SHIELD,
        listOf("A **patch** closes a hole. Some malware needs no click at all; it walks through a hole nobody patched. When there isn't time for every update, the question is **which first**. A **CVSS** score rates how **severe** a hole is compared with others, but severity is about what **could** happen.",
            "CISA keeps a **Known Exploited Vulnerabilities (KEV)** catalog: holes with **reliable evidence of attacks in the wild** and a fix available. It is meant to point effort at what is **causing harm right now**. A hole being exploited today goes before one that only **sounds** worse, and an update that only changes looks can wait."),
        listOf("cisa-kev", "first-cvss"), task = 1,
        visual = lessonEvidence("FICTIONAL UPDATE QUEUE · two slots", listOf(
            "Video player   Critical 9.1   no known attacks",
            "VPN client     High 7.8       in KEV: exploited now",
            "Mail app       High 7.5       in KEV: exploited now",
            "Theme pack     no security fix")).copy(highlights = listOf(1, 2)),
        keyTerms = listOf(KeyTerm("Patch", "An update that fixes a flaw in software."),
            KeyTerm("Known exploited", "A flaw attackers are already using, not just one that could be used."))),
    LessonPage("Allow the good, block the rest", LessonIcon.LOCK,
        listOf("NIST explains the difference. Antivirus works from a **blocklist**: stop what is **known bad**, allow everything else. **Application allowlisting** flips it: permit what is **known good** and **block everything else**, which makes it much better at stopping **malware nobody has seen yet**.",
            "Allowlists trust programs by attributes such as **publisher signature**, **hash** or **folder path**. NIST warns that a path rule is weak unless the folder is protected so that **only administrators** can add files. Allow a folder that **ordinary users can write to**, and anything a user downloads there is allowed too."),
        listOf("nist-allowlisting"), task = 2,
        visual = lessonEvidence("FICTIONAL ALLOWLIST RULES", listOf(
            "Allow  publisher = Example Office Corp.",
            "Allow  path = C:\\Program Files\\*      admins only",
            "Allow  path = C:\\Users\\*\\Desktop\\*   anyone can write",
            "One of these lets anything run.")).copy(highlights = listOf(2)),
        keyTerms = listOf(KeyTerm("Blocklist", "A list of known-bad items to stop. Anything new gets through."),
            KeyTerm("Allowlist", "A list of approved items. Everything not on it is blocked."))),
    LessonPage("Run with only what the job needs", LessonIcon.CODE,
        listOf("Saltzer and Schroeder's classic principle of **least privilege**: every program and user should work with the **fewest privileges** needed for the job. NIST notes that malware often **needs administrator rights**, so limiting them helps prevent infections.",
            "Malware runs with the rights of **whoever ran it**. Windows' **User Account Control** starts everyday apps with a **standard user** token, and admin rights need consent or a password. On a standard account a trojan can still run and read that user's files, but it can't install itself system-wide or change Windows. Least privilege **shrinks** the damage; it doesn't remove it."),
        listOf("saltzer-schroeder", "nist-malware-guide", "ms-uac"), task = 3,
        visual = lessonEvidence("FICTIONAL REPLAY · same file, standard user", listOf(
            "Run program                 allowed",
            "Read this user's Documents  allowed",
            "Install a system service    denied",
            "Change Windows settings     denied")).copy(highlights = listOf(2, 3)),
        keyTerms = listOf(KeyTerm("Least privilege", "Giving each account only the access its job needs."),
            KeyTerm("Standard user", "An account that can run programs and save its own work but can't change the system."))),
    LessonPage("Tight, but still usable", LessonIcon.WARNING,
        listOf("**Hardening** changes defaults to close doors. MITRE recommends **showing file extensions**, so a fake .pdf.exe is visible, and **turning off Autorun** for removable media. Microsoft **blocks macros in files from the internet** rather than every macro. Windows **LAPS** gives each PC its **own local admin password**, because a shared one lets an attacker move from machine to machine.",
            "Every setting also costs the people using the PC. Saltzer and Schroeder's principle of **psychological acceptability** says protection must be **easy to use**, or people won't apply it. A setting that breaks a class gets switched off or worked around, and then it protects nothing. Aim for the **tightest setting that still lets people work**."),
        listOf("mitre-double-ext", "mitre-removable", "ms-macros", "ms-laps", "saltzer-schroeder"), task = 4,
        visual = LessonVisual.DoDont(
            dos = listOf("Block macros from the internet", "Turn off Autorun, keep USB storage", "Use a unique admin password per PC"),
            donts = listOf("Block every macro when a class needs them", "Share one admin password across a lab"))),
    LessonPage("Layers whose holes don't line up", LessonIcon.SEARCH,
        listOf("Every defence has gaps. Antivirus misses new malware; patching doesn't stop a program someone chooses to run; an allowlist doesn't stop a macro inside an approved Office app. NIST describes **defence in depth** as placing **different protections along the same attack path**, so an attack that gets past one is caught by another.",
            "The #StopRansomware Guide reads the same way: **allowlisting**, **least privilege**, **patching** exploited holes, **disabling emailed macros** and **unique passwords**, all together. Match each attack to the layer that covers **how it gets in**. No single product stops them all."),
        listOf("nist-defense-depth", "cisa-ransomware-guide"), task = 5,
        visual = lessonEvidence("FICTIONAL MATCHING · how it got in → what stops it", listOf(
            "Phished password          → MFA",
            "Fake update download      → allowlist",
            "Unpatched VPN             → patching",
            "Each way in needs its own layer.")).copy(highlights = listOf(3)))
), listOf(
    LessonQuestion("Two updates fit before class. One fixes a Critical hole nobody is attacking; one fixes a High hole listed in CISA's KEV. Which goes first?",
        listOf("The Critical one, because the label is scarier", "Whichever is quicker", "The KEV one, because attackers are using it now"), 2,
        "Known exploitation beats a higher severity label when time is short."),
    LessonQuestion("Why is allowing the Downloads folder in an allowlist a trap?",
        listOf("Ordinary users can put files there, so anything downloaded would be allowed", "Downloads is too slow to scan", "It blocks Microsoft programs", "Allowlists can't use folders"), 0,
        "NIST: a path rule is only safe if only administrators can write to that folder."),
    LessonQuestion("A student runs a trojan on a standard account. What does least privilege change?",
        listOf("Nothing", "The trojan can't run at all", "The trojan runs, but can't change Windows or install itself for everyone"), 2,
        "Least privilege shrinks the damage to one profile. That's why it's one layer among several.")
))

// ---------------------------------------------------------------------------
// LEVEL 405 — Malware Identification Simulation (Lab PC 14)
// ---------------------------------------------------------------------------
internal fun malwareTriageLesson() = reviewedLesson(405, "Triage by Behaviour", listOf(
    LessonPage("Several families, one machine", LessonIcon.SEARCH,
        listOf("Real infections are rarely one neat sample. This level puts several families on **one PC**, and the job is to name each by **what it does**. NIST separates them by behaviour: a virus **infects files**, a worm **spreads on its own**, and a trojan **hides its purpose** behind a useful one.",
            "This lesson covers telling a **disguised process** from a real one, recognising a **trojan**, **containing** ransomware, spotting a **worm**, and deciding how an infected machine **returns to service**. It is all of Unit 4 in one place."),
        listOf("nist-malware-guide"),
        keyTerms = listOf(KeyTerm("Triage", "Sorting what you find by what it is and how urgently it needs action."))),
    LessonPage("The name is chosen by the attacker", LessonIcon.SEARCH,
        listOf("MITRE calls it **masquerading**: giving malware the **name or location** of something trusted, sometimes with a **swapped character**, so it blends into a list of processes. It is a **technique**, not a family: a trojan, a worm or a keylogger can all do it.",
            "NIST warns that a **file name alone** is too weak to trust, because an attacker can simply copy a common one. What is harder to fake is **where it runs from** and **who signed it**. A \"system\" process running out of a user's folder, unsigned, is lying about what it is."),
        listOf("mitre-masquerade", "nist-allowlisting"), task = 1,
        visual = lessonEvidence("FICTIONAL PROCESS LIST", listOf(
            "explorer.exe   C:\\Windows\\              Microsoft",
            "expl0rer.exe   C:\\Users\\kim\\AppData\\   unsigned",
            "Same look in the list. Different path, no signature.")).copy(highlights = listOf(1)),
        keyTerms = listOf(KeyTerm("Masquerading", "Disguising malware with a trusted-looking name or location."))),
    LessonPage("It does what it promised", LessonIcon.CODE,
        listOf("NIST defines a **trojan horse** as a non-replicating program that **seems harmless** but has a **hidden malicious purpose**. The useful function isn't an accident: software that fails its promise gets deleted, so the working part **buys the hidden part time**.",
            "Tell the families apart by **how they spread**. A virus copies itself **into other files**; a worm **spreads by itself**; a trojan does neither, it **needs you to run it**. What it does once running varies. A common choice is a **backdoor**, which NIST describes as a way for the attacker to **send commands** to the machine later."),
        listOf("nist-malware-guide"), task = 2,
        visual = lessonEvidence("FICTIONAL CHECKS · Timetable_Maker.exe", listOf(
            "Builds your class timetable      works",
            "Copies itself into other files   no",
            "Spreads to other PCs alone       no",
            "Keeps a connection to 203.0.113.9   yes")).copy(highlights = listOf(3))),
    LessonPage("When the files change as you watch", LessonIcon.WARNING,
        listOf("Ransomware doesn't hide once it starts: renamed files, a **note in every folder**, a countdown. Every second it runs costs more files, so the #StopRansomware Guide puts **isolation** first: unplug the cable or turn off Wi-Fi, and keep the machine's evidence intact for investigators.",
            "Paying, deleting the note, or wiping the disk on the spot are **not** containment. The FBI warns that paying **doesn't guarantee** your data back and **encourages more attacks**. Files come back from a **backup the malware couldn't reach**."),
        listOf("cisa-ransomware-guide", "fbi-ransomware-psa"), task = 3,
        visual = LessonVisual.DoDont(
            dos = listOf("Disconnect it from the network", "Leave the disk and the note as they are", "Report it"),
            donts = listOf("Pay to make it stop", "Reformat the drive right away"))),
    LessonPage("Nobody clicked anything", LessonIcon.LINK,
        listOf("NIST describes a **worm** as **self-contained** and usually **running without user action**, while a virus needs a host file and a person. That's why worms move at **machine speed**. In 2017, US-CERT reported that **WannaCry** spread by scanning for machines with **SMB port 445** open and exploiting an **unpatched** Windows flaw (MS17-010).",
            "The fixes in that alert weren't user training, because no user was involved: **apply the patch**, **segment the network**, and **limit share permissions and privileged accounts**. A lab where every PC shares one image and one password is ideal ground for a worm. Reading the **strings** in a sample is how analysts learn what it targets."),
        listOf("nist-malware-guide", "cisa-wannacry"), task = 4,
        visual = lessonEvidence("FICTIONAL SPREAD LOG", listOf(
            "10:00:02  LIB-01  infected",
            "10:00:41  LIB-02  via shared folder, no user signed in",
            "10:01:15  LIB-03  same password as LIB-01",
            "No one opened anything.")).copy(highlights = listOf(1, 2)),
        keyTerms = listOf(KeyTerm("Worm", "Malware that copies itself to other machines over the network on its own."))),
    LessonPage("Rebuild, restore, close the hole", LessonIcon.REPORT,
        listOf("A scan that finds nothing has told you only that it **recognises** nothing; NIST notes antivirus is weak against **previously unknown** threats. NIST's recovery advice includes **rebuilding compromised hosts from scratch or known-good backups**. The #StopRansomware Guide says to **image** affected machines for evidence, rebuild from **pre-configured standard images**, and restore data from **clean, offline backups**.",
            "Then fix what let it in, or the same attack walks back in on the next identical machine. The guide's own list applies: **patch**, **least privilege**, **allowlisting**, and **unique passwords**. Evidence first, rebuild second, restore third, close the hole last."),
        listOf("nist-malware-guide", "cisa-ransomware-guide"), task = 5,
        visual = lessonEvidence("FICTIONAL RECOVERY ORDER", listOf(
            "1  Preserve: disk image and memory capture",
            "2  Wipe and reimage from the standard image",
            "3  Restore documents from the offline backup",
            "4  Fix the share and the shared password")).copy(highlights = listOf(3)))
), listOf(
    LessonQuestion("A process named like a Windows component runs from a user's AppData folder and is unsigned. What does that tell you?",
        listOf("It is a normal part of Windows", "It is lying about what it is, but you still need its behaviour to name the family", "It must be a worm"), 1,
        "Path and signature expose the disguise. Behaviour tells you what it is."),
    LessonQuestion("Malware spread across a lab while nobody was using the PCs. Which family fits?",
        listOf("Trojan", "Virus", "Adware", "Worm"), 3,
        "Worms spread by themselves over the network. Viruses and trojans need a person."),
    LessonQuestion("A scan of an infected PC now reports zero threats. Is it safe to return to service?",
        listOf("No: rebuild it from a known-good image and restore from a clean backup", "Yes, the scan is clean", "Yes, after deleting the ransom note"), 0,
        "A clean scan means nothing was recognised. After a serious compromise, rebuild rather than trust the disk.")
))
