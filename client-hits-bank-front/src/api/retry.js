/**
 * @param {Function} requestFn
 * @param {Object} options
 * @param {number} options.maxRetries
 * @param {number} options.initialDelay
 * @param {Function} options.shouldRetry
 * @returns {Promise<any>}
 */
export async function withRetry(requestFn, options = {}) {
  const {
    maxRetries = 3,
    initialDelay = 1000,
    shouldRetry = (error) => error.response?.status >= 500 || error.code === 'ERR_NETWORK'
  } = options;

  let lastError;
  let delay = initialDelay;

  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      if (attempt > 0) {
        console.log(`Retry attempt ${attempt}/${maxRetries} after ${delay}ms`);
        await new Promise(resolve => setTimeout(resolve, delay));
        delay *= 2;
      }
      return await requestFn();
    } catch (error) {
      lastError = error;
      
      if (!shouldRetry(error) || attempt === maxRetries) {
        throw error;
      }
    }
  }
  throw lastError;
}