"use strict";
const DragLesson = (() => {
    function shuffle(values) {
        const result = values.slice();
        for (let index = result.length - 1; index > 0; index--) {
            const draw = crypto.getRandomValues(new Uint32Array(1))[0] % (index + 1);
            [result[index], result[draw]] = [result[draw], result[index]];
        }
        return result;
    }
    function makePattern(content, previous = "") {
        const eligible = content.zones.map((_, index) => index).filter(index =>
            content.cards.filter(card => card[1] === content.zones[index]).length >= 3 &&
            content.cards.filter(card => card[1] !== content.zones[index]).length >= 3);
        const zoneOrder = shuffle(eligible).slice(0, 1);
        const target = content.zones[zoneOrder[0]];
        const cards = shuffle([
            ...shuffle(content.cards.filter(card => card[1] === target)).slice(0, 3),
            ...shuffle(content.cards.filter(card => card[1] !== target)).slice(0, 3)
        ]);
        const signature = () => JSON.stringify([cards.map(card => card[0]), zoneOrder]);
        if (signature() === previous) cards.push(cards.shift());
        return {cards, zoneOrder, signature: signature()};
    }
    function mount(container, content, onNewPattern = null) {
        const patternKey = "unit2_sorting_" + content.topic[0];
        let previous = "";
        try { previous = sessionStorage.getItem(patternKey) || ""; } catch (_) {}
        const pattern = makePattern(content, previous);
        const cards = pattern.cards;
        try { sessionStorage.setItem(patternKey, pattern.signature); } catch (_) {}
        const placements = new Map();
        let selected = null, passed = false, locked = false;
        const cardButtons = [], zoneLists = [], zoneButtons = [];
        const add = (tag, value, parent = container) => {
            const el = document.createElement(tag); el.textContent = value; parent.appendChild(el); return el;
        };
        add("h2", "Put the lesson into practice");
        add("p", "Choose the three cards that match: " + content.zones[pattern.zoneOrder[0]] + ".");
        add("p", "There are six choices and exactly three correct matches. Tap a card, then tap the destination. Leave the other three in the tray. You can return a card or clear your choices before checking.");
        const counter = add("p", "0 of 3 selected");
        counter.className = "selection-count";
        const status = add("p", "Choose a card to begin.");
        status.className = "workspace-status"; status.setAttribute("role", "status"); status.setAttribute("aria-live", "polite");
        const layout = add("div", ""); layout.className = "sorting-layout";
        const bank = add("section", "", layout); bank.className = "card-bank";
        add("h3", "Card tray", bank);
        const emptyTray = add("p", "Three cards selected. Check your choices, or return a card to change it.", bank);
        const destinations = add("section", "", layout); destinations.className = "destinations";
        add("h3", "Destinations", destinations);
        function render() {
            cards.forEach((card, index) => {
                cardButtons[index].hidden = placements.has(index);
                cardButtons[index].setAttribute("aria-pressed", String(selected === index));
                cardButtons[index].setAttribute("aria-label", "Select card: " + card[0] +
                    (placements.has(index) ? ". Placed in " + content.zones[placements.get(index)] : ". Not placed"));
            });
            pattern.zoneOrder.forEach(zoneIndex => {
                const zone = content.zones[zoneIndex];
                zoneLists[zoneIndex].replaceChildren();
                const placed = cards.filter((_, index) => placements.get(index) === zoneIndex);
                if (!placed.length) add("p", "No cards placed here yet.", zoneLists[zoneIndex]);
                cards.forEach((card, index) => {
                    if (placements.get(index) !== zoneIndex) return;
                    const entry = add("div", "", zoneLists[zoneIndex]); entry.className = "placed-entry";
                    add("p", card[0], entry);
                    const undo = add("button", "Return to tray", entry); undo.type = "button"; undo.className = "return-card";
                    undo.setAttribute("aria-label", "Return card to tray: " + card[0]); undo.disabled = locked;
                    undo.addEventListener("click", () => remove(index));
                });
            });
            emptyTray.hidden = placements.size !== 3;
            counter.textContent = placements.size + " of 3 selected";
        }
        function remove(index) {
            if (locked || !placements.has(index)) return;
            placements.delete(index); passed = false; selected = null;
            render(); status.textContent = "Returned " + cards[index][0] + " to the tray. Place and check it again before finishing.";
            cardButtons[index].focus?.();
        }
        function clear() {
            if (locked) return;
            placements.clear(); passed = false; selected = null;
            render(); status.textContent = "All cards returned to the tray. Your attempt keeps the same choices; reopening the activity generates a new pattern.";
        }
        function pick(index) {
            if (locked) return;
            selected = index; render();
            status.textContent = "Selected: " + cards[index][0] + ". Choose a destination.";
        }
        function place(index, zoneIndex) {
            if (locked || !Number.isInteger(index) || index < 0 || index >= cards.length || !Number.isInteger(zoneIndex) || zoneIndex < 0 || zoneIndex >= content.zones.length) return;
            if (zoneIndex !== pattern.zoneOrder[0]) return;
            if (!placements.has(index) && placements.size >= 3) {
                status.textContent = "You already selected three cards. Return one before choosing another.";
                return;
            }
            placements.set(index, zoneIndex); passed = false; selected = null;
            status.textContent = "Placed " + cards[index][0] + " in " + content.zones[zoneIndex] + ". Check your arrangement when ready.";
            render();
        }
        cards.forEach((card, index) => {
            const button = add("button", "", bank); button.type = "button";
            button.className = "drag-card";
            add("span", card[0], button); cardButtons.push(button);
            button.addEventListener("click", () => pick(index));
        });
        pattern.zoneOrder.forEach(zoneIndex => {
            const zone = content.zones[zoneIndex];
            const wrapper = add("div", "", destinations); wrapper.className = "drop-group";
            const target = add("button", "Place selected card: " + zone, wrapper); target.type = "button";
            target.dataset.dropZone = String(zoneIndex); target.className = "drop-zone"; zoneButtons[zoneIndex] = target;
            const list = add("div", "", wrapper); list.className = "placed-cards"; zoneLists[zoneIndex] = list;
            target.addEventListener("click", () => {
                if (selected === null) { status.textContent = "Select a card first, then choose this destination."; return; }
                place(selected, zoneIndex);
            });
        });
        const reset = add("button", "Clear placements"); reset.type = "button";
        reset.addEventListener("click", clear);
        const fresh = add("button", "New card pattern"); fresh.type = "button"; fresh.hidden = !onNewPattern;
        fresh.addEventListener("click", () => { if (!locked && onNewPattern) onNewPattern(); });
        const check = add("button", "Check my three choices"); check.type = "button"; check.className = "primary";
        function validate() {
            if (locked) return passed;
            if (placements.size !== 3) { status.textContent = "Choose exactly three matching cards before checking."; passed = false; return false; }
            const wrong = cards.filter((card, index) => placements.has(index) && content.zones[placements.get(index)] !== card[1]);
            passed = wrong.length === 0;
            status.textContent = passed ? "All three choices match. Continue with the practical activity below." :
                "Return these cards and try another choice: " + wrong.map(card => card[0] + " — " + card[2]).join(" ");
            return passed;
        }
        check.addEventListener("click", validate);
        render();
        return {cards, zoneOrder: pattern.zoneOrder, remove, clear, place, validate, get passed() { return passed; }, lock() {
            locked = true; selected = null;
            cardButtons.forEach(button => { button.disabled = true; });
            zoneButtons.forEach(button => button.disabled = true); check.disabled = true; reset.disabled = true; fresh.disabled = true; render();
        }};
    }
    return {mount, makePattern};
})();
