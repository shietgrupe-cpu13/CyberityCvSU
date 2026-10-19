'use strict';
// Follow-up decisions use the same fictional case as the practical exercise.
// Each task has one practical problem plus the authored decisions below.
const TaskProblems = (() => {
  const q = (prompt, answer, distractor, explanation) => ({prompt, choices:[answer,distractor], answer:0, explanation});
  const sets = {
    '501_0': [q('DNS returned 192.0.2.20. Which machine hosts the portal in this trace?', '192.0.2.20', 'The resolver at 192.0.2.53', 'The DNS server answers the lookup; the returned address identifies the destination.'), q('The name lookup worked but TCP 443 was blocked. Did DNS prove enrollment works?', 'No. The application connection must also succeed.', 'Yes. Resolving a name proves every later step works.', 'Name resolution and application reachability are different checks.')],
    '501_1': [q('Which port identifies the portal service in the shown TCP flow?', 'Destination 443', 'Client source 53144', 'The temporary source port identifies the client side of this conversation.'), q('Can a familiar service port alone prove the connection is safe?', 'No. Also inspect the host, protocol and authenticated identity.', 'Yes. Port 443 guarantees university ownership.', 'A port is a service clue, not proof of the server’s ownership or trustworthiness.')],
    '501_2': [q('The portal works after a repair. What negative test must still pass?', 'Unsolicited inbound SMB remains blocked.', 'All inbound ports accept connections.', 'A safe repair restores the needed service without opening unrelated access.')],
    '502_0': [q('An access point has the university name but uses obsolete protection. Which evidence matters?', 'Its actual security mode and verified configuration.', 'Its familiar SSID alone.', 'A network name can be copied and does not establish security.')],
    '502_1': [q('Why use the approved transition configuration for this inventory?', 'It supports the older WPA2-AES readers while newer clients use WPA3.', 'WPA3-only always supports every old reader.', 'Choose controls that meet the supplied device requirements and plan legacy migration.'), q('Should the factory passphrase and WPS remain enabled?', 'No. Use a unique passphrase and disable WPS.', 'Yes. Strong encryption makes those settings irrelevant.', 'Wireless security also depends on provisioning and access settings.')],
    '502_2': [q('Does giving guests a different SSID guarantee isolation?', 'No. Enforce boundaries and test access to staff resources.', 'Yes. The name itself blocks routing.', 'Segmentation requires an enforced policy, not just a new label.'), q('What should a successful guest test show?', 'Approved internet works; staff and peer access stays blocked.', 'All traffic, including approved internet, is blocked.', 'The policy must preserve intended service while enforcing the boundary.')],
    '503_0': [q('An observer sees an HTTPS destination. Does that reveal the submitted form body?', 'No. TLS protects the application content in transit.', 'Yes. Seeing a connection reveals all fields.', 'Connection metadata and protected content are different kinds of evidence.')],
    '503_1': [q('The certificate does not authenticate the requested hostname. What is the next step?', 'Stop and verify through ITSO on a trusted connection.', 'Bypass the warning to test a password.', 'Do not send credentials before the intended endpoint is authenticated.')],
    '503_2': [q('A lookalike domain has valid TLS. What does that establish?', 'An encrypted authenticated connection to that domain, not university ownership.', 'Proof that every logo on the page is genuine.', 'TLS identity applies to the actual hostname, which still needs to be the intended one.'), q('Which is the safer portal entry point here?', 'The independently verified portal bookmark.', 'The unsolicited lookalike login link.', 'Use an established trusted address instead of testing a suspicious page with credentials.')],
    '504_0': [q('The replay shows protected traffic metadata. What belongs in the report?', 'The observed metadata, with no claim that protected contents were read.', 'A claim that every encrypted password was stolen.', 'Reports should distinguish observed facts from unsupported conclusions.')],
    '504_1': [q('Why prepare trusted bindings before enabling ARP inspection?', 'Inspection needs reliable IP-to-MAC mappings to reject forgeries.', 'Any ARP reply is trustworthy by default.', 'Bad or missing bindings can also disrupt legitimate traffic.')],
    '504_2': [q('Why check TLS identity after correcting the resolver?', 'A correct route still needs authentication of the intended server.', 'DNS repair makes certificate validation unnecessary.', 'Name resolution and endpoint authentication provide different protections.')],
    '550_0': [q('Two access points share an SSID. What supports identifying the rogue one?', 'The inventory, radio identifier and physical uplink evidence together.', 'The duplicated name alone.', 'Legitimate deployments can share names. Correlate independent evidence.')],
    '550_1': [q('What should the rogue AP report preserve?', 'Radio identity, location, uplink and observed impact.', 'Only a guess based on the SSID.', 'A report needs evidence that ITSO can verify and act on.')],
    '550_2': [q('The official network cannot validate the expected server. What next?', 'Stop and obtain the official profile over another trusted connection.', 'Disable validation and reconnect anyway.', 'Identity validation prevents a lookalike network from collecting credentials.'), q('What should happen to the unapproved saved profile?', 'Forget it so the device does not reconnect automatically.', 'Keep it preferred for convenience.', 'Removing the saved profile reduces accidental reconnection to the untrusted network.')],
    '505_0': [q('A broad web exception restores the portal. Why is it still wrong here?', 'It permits traffic beyond the approved destination and service.', 'Every rule that restores connectivity satisfies the policy.', 'Least privilege means permitting the required flows while denying unrelated ones.')],
    '505_1': [q('A broad allow rule precedes a narrow deny. Which rule wins in this first-match firewall?', 'The earlier matching allow rule.', 'The later rule because it is more specific.', 'First-match evaluation stops at the first applicable rule; ordering changes the outcome.'), q('After removing the broad rule, which tests are needed?', 'Approved flows pass and prohibited flows fail.', 'Only confirm the rule list looks shorter.', 'Behavioral tests show whether the repaired order enforces the intended policy.')],
    '505_2': [q('Why allow established replies while denying unsolicited inbound connections?', 'Replies belong to approved connections; unsolicited requests do not.', 'Every inbound packet is part of an approved connection.', 'State tracking distinguishes response traffic from new inbound attempts.'), q('What must the final report verify?', 'The earlier policy and rule-order work plus the final positive and negative tests.', 'Only the final page in isolation.', 'The operations report covers the complete case, including prerequisite repairs.')]
  };
  function mount(root, key, onComplete, saved = 0, onProgress = () => {}) {
    const problems = sets[key];
    if (!problems) throw new Error('Missing task problems: ' + key);
    let index = Math.min(Math.max(saved, 0), problems.length);
    const section = document.createElement('section');
    section.className = 'card task-problems';
    section.style.cssText = 'margin:16px 0;padding:16px;border:1px solid #57899c;border-radius:12px;';
    root.appendChild(section);
    function add(tag, text) {const el=document.createElement(tag); el.textContent=text; section.appendChild(el); return el;}
    function render() {
      section.replaceChildren();
      const track=add('div',''); track.className='task-progress-track';
      track.setAttribute('role','progressbar'); track.setAttribute('aria-valuemin','0');
      track.setAttribute('aria-valuemax',String(problems.length+1)); track.setAttribute('aria-valuenow',String(index+1));
      track.setAttribute('aria-label','Problems completed');
      const fill=document.createElement('div'); fill.className='task-progress-fill';
      fill.style.width=((index+1)/(problems.length+1)*100)+'%'; track.appendChild(fill);
      add('h2', 'Task progress · ' + (index + 1) + ' / ' + (problems.length + 1) + ' problems complete');
      if (index === problems.length) {add('p','All problems verified. Return to the task panel to finish this task.'); onComplete(); return;}
      const problem = problems[index];
      add('h3','Problem ' + (index + 2) + ' · Apply your finding');
      add('p',problem.prompt);
      const feedback=add('p','Choose the action supported by this case.'); feedback.setAttribute('role','status');
      const buttons=[];
      // Alternate option order so the correct position is not fixed.
      const order = (Number(key.split('_')[0]) + index) % 2 ? [1,0] : [0,1];
      let next;
      let accepted=false;
      order.forEach(choice => {
        const button=add('button',problem.choices[choice]); buttons.push(button);
        button.className='problem-choice';
        button.style.cssText='display:block;width:100%;margin:10px 0;padding:12px;white-space:normal;text-align:left;';
        button.addEventListener('click',()=>{
          if (choice !== problem.answer) {feedback.textContent='Review the case and try again. ' + problem.explanation; return;}
          feedback.textContent='Correct. ' + problem.explanation;
          accepted=true;
          button.className='problem-choice selected';
          buttons.forEach(el=>el.disabled=true);
          next.textContent=index === problems.length-1 ? 'FINISH TASK PROBLEMS' : 'NEXT PROBLEM';
          next.hidden=false;
        });
      });
      next=add('button','NEXT PROBLEM'); next.hidden=true; next.className='problem-next';
      next.addEventListener('click',()=>{if (!accepted) return; accepted=false; index++; onProgress(index); render();});
    }
    render();
    return section;
  }
  return {sets,mount};
})();
if (typeof module !== 'undefined') module.exports=TaskProblems;
