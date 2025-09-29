// src/utils/isOpen.js
const dayOfWeekNames = [
  "SUNDAY",
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
];

/**
 * @param {Object.<string, { openingTime: string, closingTime: string }>} openingHours
 *   A szerverről jövő map, pl. { MONDAY: {openingTime:"08:00:00", closingTime:"22:00:00"}, … }
 * @returns {{ openingTime: string, closingTime: string, label: string } | null}
 *   Ha ma nincs adat, null, egyébként { openingTime:"08:00", closingTime:"22:00", label:"08:00–22:00" }
 */
export function getTodayHours(openingHours) {
  const todayIndex = new Date().getDay(); // 0=Sunday … 6=Saturday
  const key = dayOfWeekNames[todayIndex];
  const hoursDto = openingHours?.[key];
  if (!hoursDto || !hoursDto.openingTime || !hoursDto.closingTime) {
    return null;
  }
  // feltételezzük ISO formátum: "HH:mm:ss" vagy "HH:mm"
  const normalize = (t) => (t.length >= 5 ? t.substr(0, 5) : t);
  const opening = normalize(hoursDto.openingTime);
  const closing = normalize(hoursDto.closingTime);
  // Ha azonos perc-pontos időpontok, akkor zárva van ma
  if (opening === closing) {
    return null;
  }
  return {
    openingTime: opening,
    closingTime: closing,
    label: `${opening}–${closing}`,
  };
}

/**
 * @param {Object.<string, { openingTime: string, closingTime: string }>} openingHours
 * @returns {boolean} true ha éppen a nyitvatartási időben vagyunk, különben false
 */
export function isRestaurantOpenNow(openingHours) {
  const today = getTodayHours(openingHours);
  if (!today) return false;
  const now = new Date();
  const pad = (num) => num.toString().padStart(2, "0");
  const nowStr = `${pad(now.getHours())}:${pad(now.getMinutes())}`;
  return nowStr >= today.openingTime && nowStr <= today.closingTime;
}
