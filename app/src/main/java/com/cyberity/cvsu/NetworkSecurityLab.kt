package com.cyberity.cvsu

/** Unit 05 content only; navigation, attempts, hearts and XP belong to LabScreen. */
private data class NetworkTask(
    val title: String,
    val objective: String,
    val guide: List<String>,
    val steps: List<String>,
    val options: List<String>,
    val correct: Int,
    val success: String,
    val hint: String
)

private data class NetworkModule(
    val folder: String,
    val title: String,
    val pages: List<String>,
    val briefing: String,
    val tasks: List<NetworkTask>
)

private val networkModules = mapOf(
    501 to NetworkModule("network_basics", "Campus Packet Desk", listOf("trace.html", "ports.html", "repair.html"),
        "The library PC can reach some services but not others. Trace a request, identify its destination service, then repair the path without opening unrelated ports.", listOf(
        NetworkTask("Follow the request", "Trace the library PC's portal request. Identify what DNS does before the first connection.",
            listOf("**Packets** carry small pieces of data between machines. An IP address identifies a network destination; a **port** identifies a service at that destination.", "**DNS** looks up the address for a name. A switch connects devices on a local network; a router forwards traffic between networks. DNS resolution is not proof that a website is trustworthy."),
            listOf("Open **Trace** from the console. Read Maya's report and inspect the route evidence.", "Tap **ADVANCE ONE HOP** to walk the request through the PC, DNS, router and portal.", "Run **REPLAY REQUEST**. Once the checks finish, swipe the bar down and choose the role of DNS."),
            listOf("Encrypts every packet", "Looks up the IP address for the portal name", "Makes any website trustworthy", "Blocks all hostile traffic"), 1,
            "DNS returned 192.0.2.20; the PC then connected to that address. Name resolution and the connection are separate steps.", "Compare the DNS answer with the destination of the connection."),
        NetworkTask("Find the service", "Inspect both connection records and select the destination port used by the portal's HTTPS service.",
            listOf("TCP connections include **source and destination ports**. The PC's temporary source port is not the server's service port.", "HTTPS commonly uses TCP **443**; DNS commonly uses UDP or TCP **53**. A port number suggests a service, but is not a guarantee that its traffic is safe."),
            listOf("Open **Ports** and inspect both records.", "Run **COMPARE CONNECTIONS**.", "Submit the HTTPS destination port in the task panel."),
            listOf("53144, the client's temporary port", "53, the DNS service", "443, the portal's destination port", "Every port is the same service"), 2,
            "The portal connection is 192.0.2.10:53144 to 192.0.2.20:443. The temporary source port lets replies reach the right client connection.", "Read the destination column, not the source column."),
        NetworkTask("Repair the path", "Restore portal access while keeping unsolicited file-sharing traffic blocked. Verify both tests, then explain the repair.",
            listOf("A firewall applies rules to traffic. **Default deny** blocks traffic unless a rule permits it. Narrow rules preserve the services people need without exposing unrelated ones.", "A **stateful** firewall recognises replies to permitted connections. You do not need to open every inbound port just to receive replies."),
            listOf("Inspect **Repair**: read the failure log and service request.", "Choose the narrow rule and **TEST BOTH FLOWS**.", "Confirm the portal works and the unsolicited connection is blocked."),
            listOf("Disable the firewall", "Allow unsolicited inbound SMB from anyone", "Permit every destination port", "Permit the portal connection and its established replies"), 3,
            "The portal is reachable over TCP 443; unsolicited SMB remains blocked. A working network and a narrow policy can coexist.", "The portal needs TCP 443, not an inbound file-sharing exception.")
    )),
    502 to NetworkModule("wifi_security", "Library Wireless Controller", listOf("audit.html", "upgrade.html", "guests.html"),
        "Prepare the library's wireless network for students. Check its current security, choose a compatible encrypted configuration, and keep visitors away from staff records.", listOf(
        NetworkTask("Audit the radio", "Read the three wireless profiles, replay their link protection, and identify the legacy setting to retire.",
            listOf("Traditional **open Wi-Fi** does not encrypt the wireless link. **WEP** and original WPA/TKIP are obsolete protections. WPA2 with AES/CCMP and WPA3 provide stronger protection.", "Wireless encryption protects the radio link, not every hop to a website. HTTPS still matters. A familiar Wi-Fi name does not prove who operates it."),
            listOf("Inspect all three profiles in **Audit**.", "Tap **REPLAY LINK PROTECTION**.", "Choose which legacy setting must be retired."),
            listOf("WEP", "WPA3-Personal", "WPA2-AES/CCMP", "HTTPS"), 0,
            "WEP is obsolete. The ordinary open profile has no link encryption; WPA2-AES protects its radio link, but does not certify website identity.", "Look for the profile marked legacy, not merely the one without a password."),
        NetworkTask("Upgrade without a lockout", "Configure a WPA3-capable library AP for a mixed fleet that still includes WPA2-only devices. Pass both compatibility and security tests.",
            listOf("Prefer **WPA3** when supported. In this exercise the fleet includes WPA2-only readers, so approved **WPA2/WPA3 transition mode** keeps them connected while newer devices use WPA3.", "Transition mode retains WPA2 compatibility and its limitations. Use a strong unique passphrase, AES/CCMP, and disable **WPS**. A WPA3-only migration is a later step after replacing incompatible clients."),
            listOf("Read the **Upgrade** inventory and policy.", "Set encryption, passphrase policy and WPS.", "Tap **TEST THE FLEET**; repair any failing result."),
            listOf("WEP preserves compatibility safely", "Use approved transition mode with a unique passphrase and WPS off", "Use the default shared password forever", "Turn encryption off to avoid support calls"), 1,
            "Both device groups connect with protected radio links. The WPA2 clients remain a migration concern, so the audit records a replacement plan.", "The old readers cannot use WPA3-only. Do not solve compatibility by removing encryption."),
        NetworkTask("Separate the guests", "Place visitors on an isolated guest network. Prove that internet access works while staff shares stay inaccessible.",
            listOf("A guest network should have its own segment and **firewall policy**. A separate SSID alone is not isolation.", "Block guest access to staff systems and other guests where appropriate. Keep the internet services visitors need. Disable automatic joining of unknown networks."),
            listOf("Inspect the **Guests** route and staff-share test.", "Select the isolation policy and **REPLAY GUEST TRAFFIC**.", "Choose why isolation matters."),
            listOf("It makes guest devices trustworthy", "It replaces HTTPS", "It limits where a compromised visitor device can connect", "It guarantees there can be no malware"), 2,
            "Visitors can browse, but cannot reach the registrar share or other guest devices. Segmentation limits exposure; it does not remove every threat.", "Watch which destinations the guest can still reach.")
    )),
    503 to NetworkModule("safe_browsing", "Sandbox Browser Bench", listOf("traffic.html", "certificate.html", "identity.html"),
        "A student is browsing from a shared network. Compare encrypted and unencrypted requests, investigate a certificate warning, and distinguish a secure connection from a trustworthy destination.", listOf(
        NetworkTask("What can the observer read?", "Inspect both request captures and replay the observer view. Identify which content valid HTTPS protects in transit.",
            listOf("**HTTPS** uses TLS to protect confidentiality and integrity between the browser and the authenticated server. Ordinary HTTP exposes request content to a network observer.", "HTTPS does not hide all metadata: destination IPs, timing and sizes can remain visible. It does not protect data after it reaches the server or a compromised device."),
            listOf("Inspect both captures in **Traffic**.", "Tap **COMPARE OBSERVER VIEWS**.", "Choose what is protected in the valid HTTPS capture."),
            listOf("Every network detail including destination IP", "The request body and page content in transit", "The website's honesty", "Files already stolen from the device"), 1,
            "The HTTP form is readable; the HTTPS application content is encrypted. The observer still sees connection metadata.", "Compare content visibility with the destination IP that remains on screen."),
        NetworkTask("Stop at the warning", "Read the requested hostname and presented certificate. Handle the warning without sending a login.",
            listOf("A certificate binds a public key to a name through a trusted validation chain. A **hostname mismatch** means the browser cannot authenticate this connection as the requested site.", "Warnings can result from mistakes or interception; the warning alone does not prove an attacker. Stop, use a known address on a trusted connection, and report persistent warnings. Do not install a certificate offered by an unknown hotspot."),
            listOf("Inspect **Certificate** and compare its two names.", "Choose **STOP AND VERIFY THROUGH ITSO**.", "Do not bypass the warning; that costs a heart."),
            listOf("Ignore it because the address starts with HTTPS", "Install the hotspot's certificate", "Stop and verify through a trusted channel", "Send a password to see whether it works"), 2,
            "No login was sent. The certificate covered hotspot-login.example, not portal.campus.example. Verification must happen outside the questionable connection.", "Compare the requested hostname with the certificate's covered name."),
        NetworkTask("A lock on a fake door", "Inspect the address, certificate and sign-in request of the lookalike page. Return to the known portal without entering credentials.",
            listOf("A valid HTTPS connection can lead to a **phishing site**. The certificate authenticates the name you reached; it does not promise that the name belongs to your university.", "Read the actual hostname, not logos or words in the path. Use a known bookmark or independently verified official address for sign-in."),
            listOf("Read all three records in **Identity**.", "Choose **OPEN THE KNOWN BOOKMARK**.", "Explain why the valid certificate did not make the lookalike safe."),
            listOf("HTTPS authenticates the reached hostname, not the site's university claim", "Any valid certificate proves the university owns it", "A logo is stronger evidence than the hostname", "A longer URL is always safer"), 0,
            "The connection was encrypted to campus-login.example, a different domain. The known bookmark avoids the lookalike without submitting a credential.", "Compare portal.campus.example with campus-login.example.")
    )),
    504 to NetworkModule("network_threats", "Campus Network Watch", listOf("capture.html", "gateway.html", "resolver.html"),
        "Students report strange traffic and portal redirects. Use packet and baseline evidence to distinguish passive observation, a forged local gateway mapping, and a DNS change before selecting targeted defenses.", listOf(
        NetworkTask("Passive observation", "Inspect the observer capture and its limits, then run the replay. Identify the threat shown without assuming HTTPS was decrypted.",
            listOf("**Sniffing** is observing network traffic. Plaintext HTTP can expose content to an observer able to see the packets. This replay models that visibility; it does not scan a real network.", "Valid TLS protects application content from a passive observer. Encrypted packets are not proof that every destination is safe."),
            listOf("Inspect **Capture** and its two visibility records.", "Run **REPLAY OBSERVER**.", "Name the action shown by the capture."),
            listOf("Ransomware", "Passive packet sniffing", "A strong password", "An automatic TLS decryption"), 1,
            "The observer read an HTTP message and saw only metadata for valid HTTPS. There is no evidence here of TLS being broken.", "The observer is reading, not altering the traffic."),
        NetworkTask("Who became the gateway?", "Compare the approved gateway mapping with the observed ARP advertisement. Apply the authorized switch defense and verify the mapping.",
            listOf("**ARP** maps an IPv4 address to a local link address. Forged ARP replies can make a victim send local traffic to an attacker, creating an interception position.", "Compare against an independent baseline. **Dynamic ARP inspection**, correctly configured with trusted bindings, can reject forged advertisements. Being in the path does not automatically defeat correctly validated TLS."),
            listOf("Inspect all records in **Gateway**.", "Enable the approved binding check and **REPLAY ARP ADVERTISEMENT**.", "Identify the observed technique."),
            listOf("Normal DNS resolution", "A certificate renewal", "ARP spoofing of the gateway mapping", "An antivirus update"), 2,
            "The unauthorized MAC was rejected and the approved gateway mapping retained. The lab demonstrates a local interception attempt, not proof of decrypted HTTPS.", "The gateway IP stayed the same; its advertised MAC changed."),
        NetworkTask("A different answer", "Compare the portal's approved DNS answer with the modified resolver setting. Restore the approved resolver and verify the portal and certificate checks.",
            listOf("A changed resolver can return an attacker-chosen address: **DNS redirection**. This case shows a rogue resolver setting, not proof of a poisoned recursive cache.", "Restore authorized settings and keep certificate validation enabled. DNS redirection to a server without a valid certificate for the intended name should trigger a TLS warning; never bypass it."),
            listOf("Inspect the **Resolver** baseline and current answer.", "Restore the approved resolver and **TEST NAME AND TLS**.", "Choose the defense that preserves authentication."),
            listOf("Restore approved DNS and keep TLS validation enabled", "Bypass certificate errors", "Accept whichever answer arrives fastest", "Turn off HTTPS"), 0,
            "The approved resolver again returns the portal baseline address, and the intended name passes certificate validation. Report the unauthorized setting change.", "Repair the changed resolver while keeping the browser's identity check.")
    )),
    550 to NetworkModule("rogue_ap", "Radio Investigation Desk", listOf("survey.html", "report.html", "reconnect.html"),
        "Three radios advertise the same campus name. Work from the ITSO inventory, switch-port records and a captured sign-in demand. Locate the unauthorized radio, report it, then restore a verified connection. No real wireless scanning takes place.", listOf(
        NetworkTask("The name proves nothing", "Inspect all three radios and the independent ITSO inventory. Identify the unapproved BSSID from corroborating evidence.",
            listOf("An **evil twin** copies a trusted Wi-Fi name to lure users. An SSID is a label; a strong signal or familiar name is not proof of ownership.", "A **BSSID** identifies a radio in this inventory, but can also be spoofed. Corroborate it with an independent asset list and physical switch-port evidence."),
            listOf("Inspect every record in **Survey**.", "Select a radio and **CORRELATE INVENTORY**.", "Return to the task and choose the unapproved radio."),
            listOf("02:00:00:00:05:11", "02:00:00:00:05:12", "02:00:00:00:05:99", "All radios with the same SSID are approved"), 2,
            "Radio :99 is absent from inventory and leads to an unauthorized lobby uplink. Radios :11 and :12 match the approved APs.", "Signal strength is a distraction. Cross-check the radio and its uplink."),
        NetworkTask("Document, don't sign in", "Inspect the rogue portal demand and uplink record. Submit an ITSO report with the BSSID and observed behavior; do not send a credential.",
            listOf("A captive portal may legitimately ask you to accept terms, but a demand for your campus password at an unrelated domain is a warning.", "Disconnect from the suspicious network and report the observed radio, location and behavior through a trusted channel. Do not unplug unknown equipment or test it with real credentials."),
            listOf("Read **Report** evidence.", "Select the evidence-backed incident report and **SEND SIMULATED REPORT**.", "Do not use **SIGN IN TO THE ROGUE PORTAL**; that costs a heart."),
            listOf("Approve it because the signal is strongest", "Report :99, the lobby uplink and unrelated credential demand", "Send your password as a test", "Report only the SSID and ignore the inventory"), 1,
            "ITSO received a report connecting :99, the lobby uplink and the credential demand. The student disconnected without handing over a password.", "Include identifiers and observed behavior, not just a familiar Wi-Fi name."),
        NetworkTask("Reconnect with evidence", "Forget the unapproved connection and select an inventory-approved radio with server identity validation. Verify portal access without a certificate exception.",
            listOf("For campus **enterprise Wi-Fi**, use the ITSO profile that validates the authentication server's certificate and expected name. Accepting any certificate removes that identity check.", "Forget an unapproved saved connection and disable auto-join. If identity cannot be verified, use another trusted connection and ask ITSO for the official configuration."),
            listOf("Inspect **Reconnect** policy and stored profile.", "Choose an approved connection with identity checking and **VERIFY RECONNECTION**.", "Explain what stops the copied name from winning again."),
            listOf("Always choose the strongest signal", "Accept any authentication certificate", "Use a familiar SSID as the only identity check", "Remove the rogue profile and validate the approved server identity"), 3,
            "The unapproved profile was forgotten. The approved radio and validated campus authentication server restore access without trusting a copied SSID.", "The name alone is still copyable. Keep the authentication identity check.")
    )),
    505 to NetworkModule("firewall_ops", "Enrollment Firewall Operations", listOf("policy.html", "order.html", "verify.html"),
        "Enrollment starts tomorrow. Guest clients need the portal but must not reach staff file shares. Build and replay a firewall policy, fix a rule-order trap, and prove that permitted replies work while unsolicited inbound traffic fails. All packets and rules are fictional.", listOf(
        NetworkTask("Permit the service, deny the spread", "Build a guest policy that permits approved DNS and portal HTTPS while rejecting staff SMB and an unapproved DNS destination. Replay all four packets.",
            listOf("A rule can match **source, destination, protocol and port**. A port-only rule may expose more destinations than intended.", "Start with **default deny** and permit required services narrowly. This case needs DNS to 192.0.2.53 and TCP 443 to 192.0.2.20; it has no need for staff SMB on TCP 445."),
            listOf("Read **Policy** requirements and packet queue.", "Choose DNS and HTTPS rules, keeping default deny.", "Tap **REPLAY FOUR PACKETS** and inspect each verdict."),
            listOf("Allow all guest traffic", "Allow DNS anywhere and all TCP", "Allow DNS to the approved resolver and HTTPS to the portal only", "Block everything, including enrollment"), 2,
            "Approved DNS and portal HTTPS passed. Staff SMB and unapproved DNS were denied. Availability was checked alongside containment.", "Destination matters: the approved resolver is 192.0.2.53."),
        NetworkTask("First match wins", "Repair the shadowed staff-share deny rule. Verify that the forbidden SMB packet is denied while the enrollment packet still passes.",
            listOf("This firewall uses **top-to-bottom, first-match** evaluation. An early broad allow can shadow a later deny, making the deny ineffective.", "Remove unnecessary broad allows and keep explicit service exceptions above the final deny. Check the matching rule, not just the final rule list."),
            listOf("Inspect the **Order** rules and packets. Ask the ITSO reviewer what must remain available.", "Use the rule list's **REMOVE** and **up/down** controls, or a preset, to repair the first-match policy. Keep the final deny last.", "Run **REPLAY RULE MATCHES**. Read the matching rule for each packet, then explain why the original deny did not take effect."),
            listOf("An earlier broad allow matched first", "SMB is always safe", "Deny rules always override allows", "The destination port changed itself"), 0,
            "The broad allow was removed. The SMB packet now hits deny while the exact portal exception still permits enrollment.", "Follow the rules from the top and stop at the first match."),
        NetworkTask("Prove the return path", "Replay an HTTPS connection, its established reply and an unsolicited inbound connection. Keep state tracking enabled and issue the verification report code.",
            listOf("A **stateful firewall** tracks permitted connections so that their return packets can pass. An unsolicited inbound packet is not an established reply.", "A successful test covers both required services and rejected traffic. Firewalls limit paths, but permitted HTTPS can still carry phishing or malware: retain browser, account and endpoint defenses from earlier units."),
            listOf("Read **Verify** packet states and acceptance criteria.", "Choose state tracking with unsolicited inbound denied.", "Run **VERIFY FINAL POLICY** and copy the report code into the task panel."),
            emptyList(), 0,
            "The client connection and established reply passed; the unsolicited inbound attempt failed. The report records a usable, narrow policy. Continue to Unit 06 to learn what to do when defenses fail.", "Return traffic must belong to an allowed connection, not just use a familiar port.")
    ))
)

fun networkSecurityClueLabels(levelId: Int): Map<String, String> = buildMap {
    val module = networkModules.getValue(levelId)
    module.tasks.forEachIndexed { index, task ->
        put("net_${levelId}_${index}_inspected", "Inspected evidence: ${task.title}")
        put("net_${levelId}_${index}_verified", "Verified replay: ${task.title}")
    }
    if (levelId in setOf(503, 550)) put("net_${levelId}_unsafe", "Bypassed identity protection")
}

fun networkSecurityLab(levelId: Int): LabDefinition {
    val module = networkModules.getValue(levelId)
    return LabDefinition(
        levelId = levelId,
        title = module.title,
        subtitle = "Network Security · ITSO",
        briefing = module.briefing + "\n\nYou are the ITSO trainee for this shift. Library staff, a student and an ITSO reviewer are simulated teammates: read their reports and ask about the symptoms or constraints. Open the named tool, inspect the evidence and replay your decision. The case status shows what remains to do; the console keeps your conversation, settings and replay history. When a finding is verified, swipe the simulation bar down to answer. Everything stays inside the bundled sandbox.",
        assetDir = "UNIT 05/${module.folder}",
        startPage = "desk.html?fresh=1",
        dangerousClues = if (levelId in setOf(503, 550)) listOf("net_${levelId}_unsafe") else emptyList(),
        tasks = module.tasks.mapIndexed { index, task ->
            LabTask(
                id = "t${index + 1}", title = task.title, objective = task.objective,
                guide = task.guide, steps = task.steps,
                answer = if (levelId == 505 && index == 2) LabAnswer.Flag(
                    sha256 = "98db56afb7938ad0e697bb2cb3a529236ca387cefc7c564f80249646f6ed348f"
                ) else LabAnswer.Choice(task.options, task.correct),
                requiredClues = listOf("net_${levelId}_${index}_inspected", "net_${levelId}_${index}_verified"),
                lockedMessage = "Inspect every evidence record and pass the simulation replay before answering.",
                entryPage = if (index == 0) "desk.html?fresh=1" else module.pages[index],
                hints = listOf(task.hint, "Read the replay results and compare them with the acceptance criteria."),
                successFeedback = task.success,
                failureFeedback = if (levelId == 505 && index == 2) "Use the report code displayed after the final policy passes verification."
                    else "Compare your answer with the evidence and replay results. ${task.hint}"
            )
        }
    )
}
