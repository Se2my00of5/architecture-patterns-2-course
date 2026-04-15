const TELEMETRY_TRACE_URL = 'http://localhost:5003/api/telemetry/trace';
const TELEMETRY_ERROR_URL = 'http://localhost:5003/api/telemetry/error';

class TelemetryClient {
  constructor() {
    this.queue = [];
    this.isSending = false;
    this.serviceName = this.getServiceName();
  }

  getServiceName() {
    if (window.location.port === '3000') return 'client-front';
    if (window.location.port === '3001') return 'employee-front';
    return 'unknown-front';
  }

  /**
   * @param {Object} data
   * @param {string} data.method
   * @param {string} data.path
   * @param {number} data.statusCode
   * @param {number} data.elapsedMs
   */
  trace(data) {
    const traceData = {
      id: crypto.randomUUID(),
      serviceName: this.serviceName,
      traceId: crypto.randomUUID(),
      method: data.method,
      path: data.path,
      statusCode: data.statusCode,
      elapsedMs: data.elapsedMs,
      timestamp: new Date().toISOString()
    };

    fetch(TELEMETRY_TRACE_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(traceData)
    }).catch(err => console.warn('Telemetry failed:', err));
  }

  /**
   * @param {Object} data
   * @param {string} data.traceId
   * @param {string} data.errorMessage
   * @param {string} data.stackTrace
   */
  error(data) {
    const errorData = {
      id: crypto.randomUUID(),
      serviceName: this.serviceName,
      traceId: data.traceId || crypto.randomUUID(),
      errorMessage: data.errorMessage || 'Unknown error',
      stackTrace: data.stackTrace || '',
      timestamp: new Date().toISOString()
    };

    fetch(TELEMETRY_ERROR_URL, {
      method: 'POST',
      headers: {
        accept: '*/*',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(errorData)
    }).catch(err => console.warn('Telemetry error failed:', err));
  }
}

export const telemetry = new TelemetryClient();