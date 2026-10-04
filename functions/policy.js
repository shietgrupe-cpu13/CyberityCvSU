"use strict";

const RETENTION_MS = 90 * 24 * 60 * 60 * 1000;
function sessionInput(data) {
  if (!data || typeof data.sessionId !== "string" ||
      !/^[a-f0-9-]{36}$/.test(data.sessionId)) throw new Error("Invalid session ID");
  if (typeof data.device !== "string" || !data.device.trim() || data.device.length > 100 ||
      typeof data.androidVersion !== "string" || data.androidVersion.length > 30) {
    throw new Error("Invalid device details");
  }
  return { sessionId: data.sessionId, device: data.device.trim(), androidVersion: data.androidVersion };
}
function isFresh(authTime, nowSeconds) {
  return Number.isFinite(authTime) && authTime <= nowSeconds + 30 && nowSeconds - authTime <= 300;
}
function isActive(session, validAfter, nowMillis) {
  return !session.signedOut && session.authTime >= validAfter &&
    session.lastActive >= nowMillis - RETENTION_MS;
}
module.exports = { RETENTION_MS, sessionInput, isFresh, isActive };
