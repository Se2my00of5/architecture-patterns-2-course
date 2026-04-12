const State = {
  CLOSED: 'CLOSED',
  OPEN: 'OPEN',
  HALF_OPEN: 'HALF_OPEN'
};

class CircuitBreaker {
  constructor(options = {}) {
    this.failureThreshold = options.failureThreshold || 5;
    this.timeout = options.timeout || 10000;
    this.successThreshold = options.successThreshold || 3;
    
    this.state = State.CLOSED;
    this.failureCount = 0;
    this.successCount = 0;
    this.nextAttempt = null;
  }

  /**
   * @param {Function} requestFn
   * @returns {Promise<any>}
   */
  async call(requestFn) {
    if (this.state === State.OPEN) {
      if (Date.now() < this.nextAttempt) {
        throw new Error(`Circuit breaker is OPEN. Try again after ${new Date(this.nextAttempt).toLocaleTimeString()}`);
      }
      this.state = State.HALF_OPEN;
      this.successCount = 0;
      console.log('Circuit breaker: HALF_OPEN - trying to recover');
    }

    try {
      const result = await requestFn();
      this.onSuccess();
      return result;
    } catch (error) {
      this.onFailure();
      throw error;
    }
  }

  onSuccess() {
    if (this.state === State.HALF_OPEN) {
      this.successCount++;
      if (this.successCount >= this.successThreshold) {
        this.close();
      }
    } else if (this.state === State.CLOSED) {
      this.failureCount = 0;
    }
  }

  onFailure() {
    if (this.state === State.CLOSED) {
      this.failureCount++;
      if (this.failureCount >= this.failureThreshold) {
        this.open();
      }
    } else if (this.state === State.HALF_OPEN) {
      this.open();
    }
  }

  open() {
    this.state = State.OPEN;
    this.nextAttempt = Date.now() + this.timeout;
    console.warn(`Circuit breaker: OPEN for ${this.timeout / 1000}s`);
  }

  close() {
    this.state = State.CLOSED;
    this.failureCount = 0;
    this.successCount = 0;
    console.log('Circuit breaker: CLOSED');
  }

  getState() {
    return this.state;
  }
}

export const userServiceCB = new CircuitBreaker({ failureThreshold: 3, timeout: 10000 });
export const coreServiceCB = new CircuitBreaker({ failureThreshold: 3, timeout: 10000 });
export const creditServiceCB = new CircuitBreaker({ failureThreshold: 3, timeout: 10000 });