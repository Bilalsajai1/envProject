// jwt-decode.js - Simple JWT Decode Implementation (Browser/Node.js compatible)
// Based on standard implementations; does NOT validate signatures!

/**
 * Decodes a JWT token to extract the payload.
 * @param {string} token - The JWT token to decode.
 * @param {Object} [options] - Optional options.
 * @param {boolean} [options.header=false] - If true, decodes the header instead of payload.
 * @returns {Object|null} The decoded JSON object or null on error.
 */
function jwt_decode(token, options) {
  options = options || {};
  const getPart = (idx) => {
    const part = token.split('.')[idx];
    if (!part) return null;

    // Base64url to Base64
    let base64 = part.replace(/-/g, '+').replace(/_/g, '/');
    // Add padding
    while (base64.length % 4) {
      base64 += '=';
    }

    try {
      // Decode with unicode handling
      const str = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(str);
    } catch (e) {
      return null;
    }
  };

  return options.header === true ? getPart(0) : getPart(1);
}

// For ES modules/CommonJS
if (typeof module !== 'undefined' && module.exports) {
  module.exports = jwt_decode;
} else if (typeof define === 'function' && define.amd) {
  define(() => jwt_decode);
} else {
  window.jwt_decode = jwt_decode;
}

// Default export for ES6
export default jwt_decode;
