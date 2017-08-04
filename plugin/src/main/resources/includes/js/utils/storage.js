/**
 * Note: this is a copy of 
 * https://bitbucket.org/shghs/aui/src/612a10e756ce/src/storage.js.
 * 
 * Adapted slightly
 *
 */

// require AUI
// provide AUI.storage

if (typeof BF == 'undefined') { window.BF = {}; }
if (typeof BF.storage == 'undefined') { BF.storage = {}; }

/**
 * @param {string} key
 * @returns {Object|string|number|boolean}
 */
BF.storage.get = function(key) {
  var value = localStorage.getItem(key);
  if (typeof value === "string") {
    // Ensure we catch json parse exceptions as value could be a string containing invalid json
    try {
      return JSON.parse(value);
    } catch(err) {
      return undefined;
    }
  } else {
    return value;
  }
};

/**
 * @param {string} key
 * @param {Object|string|number|boolean} value
 * @returns {boolean}
 */
BF.storage.put = function(key, value) {
  if (value === null) {
    localStorage.removeItem(key);
  } else {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (QUOTA_EXCEEDED_ERR) {
      return false;
    }
  }
  return true;
};

if (!window.localStorage) {
  // When localStorage is unavailable, get and put methods always fail.
  BF.storage.get = function() {
    return null;
  };
  BF.storage.put = function() {
    return false;
  };
}