import axios from 'axios';

const TELEMETRY_URL = 'http://localhost:5003/api/telemetry/trace';

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

    fetch(TELEMETRY_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(traceData)
    }).catch(err => console.warn('Telemetry failed:', err));
  }
}

export const telemetry = new TelemetryClient();