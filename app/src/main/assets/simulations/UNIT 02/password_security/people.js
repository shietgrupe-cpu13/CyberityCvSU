"use strict";
// Scripted fictional people: no network, live chat, or real account credentials.
const UnitTwoPeople = (() => {
    const people = {
        201: {name: "Maya Santos", initials: "MS", role: "Student · portal and shopping accounts", account: "student.training@campus.example", conversation: [
            ["I added the campus name, the year, and an exclamation mark. Isn't that enough?", "Ask where else she uses it", "I used CvSU2026! on shopping too. Please give the portal its own replacement first; we will repair shopping next."],
            ["The portal has the replacement you made. What happens to my shopping account?", "Ask about the linked account", "Shopping still has the old exposed value. Keep the portal replacement, generate a different shopping secret, and store both in a protected manager."],
            ["Every month I just change the month in my password. The portal makes me reset it.", "Ask how the policy affects her", "I want to paste a generated password from my manager. The current policy makes that difficult. Allow long passwords and autofill, block known weak values, and reset after exposure."]
        ]},
        202: {name: "Jules Reyes", initials: "JR", role: "Campus IT · portal incident desk", account: "portal.campus.example", conversation: [
            ["The portal raised an alert. Help me work out what the login records actually show.", "Ask what the alert means", "Open the login evidence. One username receives several familiar guesses and all fail. The candidate source matters; a long list alone does not prove brute force."],
            ["The alert has escalated: this time some logins succeeded.", "Ask about the escalation", "The records now contain many different emails paired with leaked shopping-site passwords. Inspect the successful sessions as well as the failures before choosing your response."],
            ["We discovered that the attacker copied our credential database.", "Ask whether throttling is enough", "Those guesses happen on the attacker's computer. Portal login throttling cannot stop them. Compare the two hash examples, then repair storage and reset exposed credentials."]
        ]},
        203: {name: "Maya Santos", initials: "MS", role: "Student · sign-in protection", account: "student.training@campus.example", conversation: [
            ["Could I add a secret question as my second factor?", "Ask which device she controls", "I have my own phone and can enrol an authenticator. Another memorised answer is still something I know. Complete the training enrolment and verify its code."],
            ["My phone keeps asking me to approve a login, but I'm not signing in.", "Ask whether she started the login", "I didn't start any of these requests. Deny them, inspect the official login history, and record the suspicious attempt with campus IT."],
            ["I want a sign-in method that checks which service I'm using.", "Ask about backup access", "Register a passkey through the official portal. Keep recovery codes in a protected vault or protected offline copy, so losing a device doesn't leave me locked out."]
        ]},
        204: {name: "Maya Santos", initials: "MS", role: "Student · recovery and device access", account: "student.training@campus.example", conversation: [
            ["My backup codes ended up in a shared class folder.", "Ask who controls recovery", "My old recovery mailbox is abandoned. Choose my current protected mailbox, replace the exposed codes, and keep the new ones private. Moving old codes doesn't invalidate copied versions."],
            ["I left the library computer signed in. My own phone is still with me.", "Ask which session is trusted", "The personal phone is mine and its activity is expected. The library session should end. Preserve the phone, revoke library access, and choose sign-out for shared devices."],
            ["There is a new unfamiliar login, and my mailbox settings changed.", "Ask what changed", "Recovery now points somewhere I don't control and a forwarding rule sends mail outside. Replace the credential, end attacker access, restore recovery, remove forwarding, and record the incident."]
        ]},
        205: {name: "Elena Cruz", initials: "EC", role: "Registrar · mailbox incident", account: "registrar.training@campus.example", conversation: [
            ["Someone accessed our training mailbox. We have a copied credential record to investigate.", "Ask what evidence is available", "Use the displayed salt and complete target digest. Compare your candidate outputs yourself. Finding the weak password establishes exposure; it doesn't secure my mailbox."],
            ["You confirmed the weak credential. Please secure this same mailbox.", "Ask what has been repaired", "The old credential is still active and there is no MFA. Generate its own replacement and enrol supported sign-in protection. Existing attacker sessions need separate containment."],
            ["I still see an unfamiliar desktop session after the sign-in repair.", "Ask which access to keep", "Keep my trusted phone. Revoke the unfamiliar desktop, remove outside forwarding, restore owner-controlled recovery, rotate exposed codes, and finish the local incident record."]
        ]},
        250: {name: "Elena Cruz", initials: "EC", role: "Registrar · authorised legacy-record audit", account: "registrar.training@campus.example", conversation: [
            ["We need to assess a legacy common-word record in this authorised training audit.", "Ask about the candidate list", "The fictional secret is in the supplied common-word list. Include the salt exactly and compare the entire digest. Start the timer only when you are ready."],
            ["The next legacy record uses a campus word and a familiar suffix.", "Ask about the candidate format", "Use one of four campus words, one of three nearby years, and ! or @. That gives 24 candidates. A new attempt changes the target, so compare this record."],
            ["The last record is a two-digit training recovery PIN.", "Ask about leading zeroes", "Enumerate 00 through 99 as two-character strings. 07 and 7 hash differently. This small exercise is not an estimate of real cracking speed."]
        ]}
    };
    function mount(container, levelId, index) {
        const person = people[levelId], [request, question, answer] = person.conversation[index];
        function append(tag, content, className) {
            const element = document.createElement(tag);
            element.textContent = content;
            if (className) element.className = className;
            container.appendChild(element); return element;
        }
        append("div", person.initials, "person-avatar");
        append("p", "CAMPUS IT HELP DESK · FICTIONAL CASE", "eyebrow");
        append("h2", person.name);
        append("p", person.role, "muted");
        append("p", person.account, "person-account");
        append("blockquote", request, "person-message");
        const button = append("button", question); button.id = "ask-owner";
        button.setAttribute("aria-expanded", "false"); button.setAttribute("aria-controls", "owner-reply");
        const reply = append("p", answer, "person-reply"); reply.id = "owner-reply"; reply.hidden = true;
        button.addEventListener("click", () => {
            reply.hidden = !reply.hidden;
            button.setAttribute("aria-expanded", String(!reply.hidden));
        });
    }
    return {mount};
})();
