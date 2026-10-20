function planUsernames(profiles) {
 const groups = new Map();
 for (const profile of profiles) {
  const name = profile.displayName;
  if (typeof name !== 'string' || name.trim().length < 3 || name.trim().length > 20 || /[\/\x00-\x1f\x7f]/.test(name))
   throw Error('Invalid username for account ' + profile.uid);
  if (!Number.isFinite(profile.registeredAt)) throw Error('Missing registration date for account ' + profile.uid);
  const key = name.trim().toLowerCase();
  groups.set(key, [...(groups.get(key) || []), profile]);
 }
 return [...groups].map(([key, members]) => {
  members.sort((a,b) => a.registeredAt-b.registeredAt);
  if (members.length > 1 && members[0].registeredAt === members[1].registeredAt)
   throw Error('Registration dates tie for username ' + key + '; resolve explicitly.');
  return {key, winner:members[0], losers:members.slice(1)};
 });
}
module.exports = { planUsernames };
