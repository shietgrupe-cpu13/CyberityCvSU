"use strict";
const test = require("node:test");
const assert = require("node:assert/strict");
const { sessionInput, isFresh, isActive, RETENTION_MS } = require("./policy");
test("device IDs cannot inject document paths or oversized metadata", () => {
  const input = { sessionId: "12345678-abcd-abcd-abcd-123456789012", device: "Android", androidVersion: "16" };
  assert.equal(sessionInput(input).device, "Android");
  for (const data of [null, { ...input, sessionId: "../other-user" }, { ...input, device: "x".repeat(101) }]) {
    assert.throws(() => sessionInput(data));
  }
});
test("global logout requires authentication within five minutes", () => {
  assert.equal(isFresh(1000, 1299), true);
  assert.equal(isFresh(1000, 1301), false);
  assert.equal(isFresh(2000, 1000), false);
  assert.equal(isFresh(undefined, 1000), false);
});
test("history excludes revoked, signed-out and expired records", () => {
  const now = RETENTION_MS + 10000;
  const session = { authTime: 1000, lastActive: now, signedOut: false };
  assert.equal(isActive(session, 1000, now), true);
  assert.equal(isActive(session, 1001, now), false);
  assert.equal(isActive({ ...session, signedOut: true }, 1000, now), false);
  assert.equal(isActive({ ...session, lastActive: 0 }, 1000, now), false);
});
