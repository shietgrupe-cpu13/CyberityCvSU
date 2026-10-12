package com.cyberity.cvsu

// The evidence displays are original fictional examples, distinct from the lab's answers.
internal fun networkBasicsLesson() = reviewedLesson(501, "Follow a Request to Its Service", listOf(
    LessonPage("A name lookup is not the connection", LessonIcon.SEARCH,
        listOf("DNS resolves names using resource records, including records for host addresses. A client can then connect to an address it received. Resolution and connection are separate operations; a DNS response alone does not establish that a website deserves your trust.",
            "In the trace task, follow the evidence in order: requested name, returned address and subsequent connection. Do not confuse the machine that answers the lookup with the machine hosting the application."),
        listOf("dns"), task = 1,
        visual = lessonEvidence("FICTIONAL REQUEST TRACE", listOf("Name: catalog.training.example", "Resolver returns a destination address", "Client then starts the service connection")),
        keyTerms = listOf(KeyTerm("Resolver", "The DNS component that obtains an answer for a client's query."))),
    LessonPage("Read both ends of the connection", LessonIcon.CODE,
        listOf("TCP identifies a connection using endpoint addresses and ports. A client may choose a temporary source port, while the destination port identifies the server endpoint it requests. Swapping the source and destination columns changes your interpretation.",
            "A port's registered use is a clue, not a security verdict. Compare protocol, address, source port and destination port together in the service task. The lab's portal uses TCP; not every possible HTTPS transport is TCP."),
        listOf("tcp", "service-ports"), task = 2,
        visual = lessonEvidence("FICTIONAL CONNECTION", listOf("Client 192.0.2.31:54012", "Server 192.0.2.40:443", "Read the destination port separately from the source"))),
    LessonPage("Repair only the required path", LessonIcon.SHIELD,
        listOf("A firewall policy can restrict traffic by address, protocol and port. Default deny starts from blocked traffic and adds explicit exceptions for required services. State tracking distinguishes permitted connection replies from unsolicited inbound traffic.",
            "In the repair task, check two outcomes: the approved application works and an unrelated inbound request stays blocked. Turning off the firewall proves reachability, but does not prove the required security policy."),
        listOf("nist-firewall"), task = 3,
        keyTerms = listOf(KeyTerm("Default deny", "Traffic is blocked unless an applicable rule permits it.")))
), listOf(
    LessonQuestion("What does a successful DNS lookup establish?",
        listOf("Every packet is encrypted", "An address answer was obtained for the name", "The website is honest"), 1,
        "DNS resolution is one step; transport security and destination trust require other checks."),
    LessonQuestion("Which result verifies a narrow firewall repair?",
        listOf("The required service works and the forbidden flow stays blocked", "Every port becomes reachable", "The rule list looks longer"), 0,
        "Test required availability and denied traffic together.")
))

internal fun wifiSecurityLesson() = reviewedLesson(502, "Protect the Wireless Link and Separate Guests", listOf(
    LessonPage("Recognize obsolete wireless protection", LessonIcon.WARNING,
        listOf("Traditional open Wi-Fi provides no radio-link encryption. WEP and original WPA/TKIP are obsolete choices. Modern configuration guidance favors WPA3, with appropriate WPA2-AES compatibility when older clients require it.",
            "Radio protection is not end-to-end website protection. Inspect each profile's actual security mode rather than its familiar network name. The audit task asks which protection is obsolete, not which name sounds most official."),
        listOf("apple-wifi"), task = 1,
        keyTerms = listOf(KeyTerm("Wireless link", "The radio connection between a client and access point."))),
    LessonPage("Migrate without removing protection", LessonIcon.SHIELD,
        listOf("WPA2/WPA3 transitional mode lets compatible clients use WPA3 while older clients use WPA2-Personal with AES. It is a compatibility compromise, not the same as every client using WPA3. Select a mode based on the actual device inventory.",
            "For this lab, test both the older and newer device groups against the supplied policy. If a secure upgrade excludes old hardware, record that migration constraint instead of making the whole network unencrypted."),
        listOf("apple-wifi"), task = 2,
        visual = lessonEvidence("FICTIONAL FLEET", listOf("New tablets: modern mode supported", "Older readers: compatibility needed", "Validate protection and connection for both groups"))),
    LessonPage("A guest name is not an access boundary", LessonIcon.LOCK,
        listOf("Wireless security includes the paths from the WLAN into other networks. Separate guest and internal segments, with access controls between them, limit which systems a visitor can reach. Naming an SSID 'Guest' does not itself enforce isolation.",
            "In the guest task, prove both sides of the policy: visitors retain approved internet access, while staff resources remain inaccessible. A useful segment has an enforced boundary, not just a different label."),
        listOf("nist-wlan"), task = 3,
        visual = LessonVisual.DoDont(listOf("Test guest access to permitted and restricted destinations"), listOf("Assume a separate SSID alone blocks staff access")))
), listOf(
    LessonQuestion("What is the limitation of WPA2/WPA3 transitional mode?",
        listOf("It encrypts no clients", "It automatically proves every site's identity", "Older clients still use WPA2 rather than WPA3"), 2,
        "Transition mode supports a mixed fleet; it does not upgrade an incompatible client."),
    LessonQuestion("What makes guest isolation meaningful?",
        listOf("Enforced access controls and verification tests", "A friendly SSID", "A stronger radio signal"), 0,
        "Segmentation must restrict actual network paths.")
))

internal fun safeBrowsingLesson() = reviewedLesson(503, "Separate Encryption from Trust", listOf(
    LessonPage("What TLS hides—and what it does not", LessonIcon.LOCK,
        listOf("HTTPS uses TLS to protect application content in transit from reading and alteration. Passive observers can still learn metadata such as traffic timing and sizes; encryption does not hide every network fact.",
            "Compare content visibility in the traffic task. A private form body is different from an invisible connection. TLS also does not clean an infected endpoint or protect data after the intended server receives it."),
        listOf("tls"), task = 1,
        visual = lessonEvidence("FICTIONAL OBSERVER", listOf("HTTP: form content visible", "Valid HTTPS: application content protected", "Connection timing remains observable"))),
    LessonPage("Check the requested name against the certificate", LessonIcon.WARNING,
        listOf("TLS service authentication compares the intended service identity with identifiers in its certificate. A certificate for one hostname does not automatically authenticate a different hostname. The name check is part of the protection, not an optional inconvenience.",
            "At a warning, stop before entering credentials. Verify the official address and seek support through a trusted route. A mismatch may reflect a configuration error or interception; the warning alone does not prove which occurred."),
        listOf("service-id"), task = 2,
        visual = LessonVisual.Address("Intended service", "records", "training.example"),
        keyTerms = listOf(KeyTerm("Hostname mismatch", "The certificate does not identify the service name the client intended to reach."))),
    LessonPage("An encrypted impostor is still an impostor", LessonIcon.LINK,
        listOf("A certificate can validate the impostor domain you actually opened. That does not establish the site's claimed university identity. Encryption to the wrong destination is still the wrong destination.",
            "For the identity task, compare the complete hostname with an independently verified address. Logos and a university word in the path are weak evidence. Open a known bookmark rather than submit a password just to test the page."),
        listOf("service-id"), task = 3,
        visual = LessonVisual.Link("https://records.training.example", "https://records-help.example/university"))
), listOf(
    LessonQuestion("What does a valid encrypted connection establish?",
        listOf("The website's claims are honest", "All traffic metadata is hidden", "The connection protects content to the authenticated service identity"), 2,
        "TLS protects the connection; choosing the intended service remains essential."),
    LessonQuestion("A login page shows a hostname-mismatch warning. What should you do?",
        listOf("Stop and verify through a trusted route", "Submit a password to diagnose it", "Install a certificate from the unknown hotspot"), 0,
        "Do not remove the identity check before sending credentials.")
))

internal fun networkThreatsLesson() = reviewedLesson(504, "Match Network Evidence to the Threat", listOf(
    LessonPage("Observation is different from alteration", LessonIcon.SEARCH,
        listOf("Passive sniffing reads traffic that the observer can see. It does not by itself mean packets were changed or properly validated TLS was decrypted. Separate exposed plaintext from the metadata left visible around encrypted content.",
            "The capture task is a replay of fictional evidence. Describe what the observer actually obtained, and avoid upgrading a visible connection into a claim that all its protected contents were stolen."),
        listOf("tls", "nist-wlan"), task = 1,
        keyTerms = listOf(KeyTerm("Passive observation", "Reading available traffic without changing it."))),
    LessonPage("Verify the local gateway mapping", LessonIcon.WARNING,
        listOf("ARP resolves an IPv4 address to a local link-layer address. Forged advertisements can substitute an attacker's address for an expected gateway mapping. Compare observations with an independent approved baseline.",
            "Dynamic ARP inspection validates packets against trusted bindings, commonly supplied by DHCP snooping or configured rules. It needs correct bindings; switching it on without that preparation can disrupt legitimate traffic. The task models an authorized, prepared defense."),
        listOf("arp", "cisco-dai"), task = 2,
        visual = lessonEvidence("FICTIONAL BASELINE CHECK", listOf("Gateway IP: unchanged", "Observed MAC: differs from approved binding", "Investigate the mapping, not just the IP"))),
    LessonPage("A changed resolver is evidence of a changed path", LessonIcon.LINK,
        listOf("A client queries its configured resolver for DNS answers. If that setting is replaced, the answers may lead elsewhere. A rogue resolver configuration and a poisoned DNS cache are different mechanisms; use the evidence to name the one shown.",
            "Restore the approved configuration and retain TLS service-name validation. Check the name resolution and the authenticated connection together. A replacement server's address alone does not give it a valid identity for the requested service."),
        listOf("dns", "service-id"), task = 3,
        visual = LessonVisual.DoDont(listOf("Restore authorized DNS and retain identity validation"), listOf("Bypass TLS warnings to make a redirected page load")))
), listOf(
    LessonQuestion("Which evidence directly concerns ARP?",
        listOf("The page's title changed", "A gateway IPv4 address was advertised with an unexpected MAC", "The student's password got longer"), 1,
        "ARP concerns the local protocol-to-link address mapping."),
    LessonQuestion("A workstation uses an unauthorized DNS resolver. What can you conclude?",
        listOf("Its resolver configuration changed; investigate the resulting answers", "Every recursive cache was poisoned", "TLS was necessarily decrypted"), 0,
        "State what the evidence supports; other attack claims need their own evidence.")
))

internal fun rogueAccessPointLesson() = reviewedLesson(550, "Investigate a Radio Without Trusting Its Name", listOf(
    LessonPage("Correlate identifiers with an approved inventory", LessonIcon.SEARCH,
        listOf("Wireless monitoring can identify unauthorized access points by comparing observed devices and behavior with an approved baseline. A copied SSID or strong signal does not demonstrate ownership. A radio identifier is evidence to correlate, not an unforgeable identity.",
            "In the survey task, combine the inventory, radio record and physical uplink evidence. Do not mark every access point with the same SSID as hostile: legitimate deployments can share a network name."),
        listOf("nist-wlan"), task = 1,
        keyTerms = listOf(KeyTerm("SSID", "A wireless network name; it is not proof of operator identity."))),
    LessonPage("Report the evidence you can reproduce", LessonIcon.REPORT,
        listOf("A rogue access point may imitate an organization and create an interception position. Reports should connect the observed device with its location or uplink and suspicious behavior, rather than relying on appearance alone.",
            "For this classroom report, record the observed identifier, time, inventory discrepancy, uplink and unexpected credential demand. Disconnect from the unapproved connection; do not send a real password as a test. This exercise is not a scan of nearby real networks."),
        listOf("nist-wlan"), task = 2,
        visual = lessonEvidence("FICTIONAL REPORT OUTLINE", listOf("Observed radio and time", "Approved inventory comparison", "Uplink evidence and credential demand"))),
    LessonPage("Reconnect with a validated server identity", LessonIcon.SHIELD,
        listOf("Enterprise authentication must validate the intended authentication server's certificate and identity. Accepting any certificate removes that check. RFC 9190 describes this requirement for EAP-TLS 1.3; other enterprise methods require their own approved profile.",
            "In the reconnection task, remove the unapproved saved profile and use the ITSO configuration with identity validation. If the expected server cannot be verified, stop and obtain the official profile through another trusted connection."),
        listOf("eap-tls"), task = 3,
        visual = LessonVisual.DoDont(listOf("Use the approved profile and expected server identity"), listOf("Accept every certificate to join a familiar SSID")))
), listOf(
    LessonQuestion("Two radios advertise the same SSID. What should guide your judgment?",
        listOf("Only signal strength", "Inventory, identifiers and observed behavior together", "Only the familiar network name"), 1,
        "Correlated evidence is stronger than a copyable label."),
    LessonQuestion("Why keep server certificate validation enabled?",
        listOf("To authenticate the intended enterprise server", "To make every nearby radio authorized", "To replace all network monitoring"), 0,
        "The identity check is part of enterprise authentication, not a cosmetic setting.")
))

internal fun firewallPolicyLesson() = reviewedLesson(505, "Build and Verify a Narrow Firewall Policy", listOf(
    LessonPage("Match the service and its destination", LessonIcon.SHIELD,
        listOf("A narrow firewall exception identifies the required endpoints, protocol and port. Allowing a port to every destination can permit more traffic than the requirement calls for. State the service need before selecting a rule.",
            "In this fictional exercise, keep an explicit default-deny policy and test approved service flows alongside forbidden ones. An all-blocked configuration fails availability; an all-allowed configuration fails containment."),
        listOf("nist-firewall"), task = 1,
        visual = lessonEvidence("FICTIONAL POLICY REQUIREMENT", listOf("Guests → approved application: permit", "Guests → staff share: deny", "Other destinations: no blanket exception"))),
    LessonPage("Read the rule evaluation model", LessonIcon.CODE,
        listOf("Some firewalls evaluate rules top to bottom until a decisive match. Others use different processing models. This lab explicitly uses first-match evaluation: a broad allow above a specific deny can make the deny unreachable.",
            "Trace a sample packet from the first rule and stop where the lab says its verdict is decided. Inspect the matching rule, not just whether a deny appears somewhere in the list. Preserve required exceptions when removing an overly broad allow."),
        listOf("nist-firewall"), task = 2,
        visual = LessonVisual.Source(listOf("Fictional first-match policy:", "1. broad permit ← packet stops here", "2. specific deny ← never reached")),
        keyTerms = listOf(KeyTerm("Shadowed rule", "A rule that cannot affect the tested traffic because an earlier decisive rule matches."))),
    LessonPage("A reply needs connection context", LessonIcon.SEARCH,
        listOf("Connection tracking records flow state. An established reply belongs to a recognized connection; an unsolicited incoming packet is not made legitimate just by using a familiar port. Stateful rules use that context in their decision.",
            "Verify an allowed new connection, its return traffic and an unrelated inbound attempt. The report code in this lab comes from the successful verification. A firewall controls paths; it does not guarantee that every permitted application's content is safe."),
        listOf("netfilter-state", "nist-firewall"), task = 3,
        visual = lessonEvidence("FICTIONAL ACCEPTANCE TEST", listOf("Approved new flow → permitted", "Tracked reply → permitted", "Unsolicited inbound flow → denied")))
), listOf(
    LessonQuestion("In this lab's first-match model, why can a later deny fail to block a packet?",
        listOf("An earlier broad allow already decided its verdict", "Deny rules never work", "Port numbers automatically override policy"), 0,
        "Evaluation order matters in the declared model; other firewalls may differ."),
    LessonQuestion("What distinguishes an established reply from an unsolicited packet?",
        listOf("The reply uses any popular port", "Its relationship to a tracked permitted connection", "It has a longer payload"), 1,
        "State tracking checks the flow context rather than trusting a port label.")
))
