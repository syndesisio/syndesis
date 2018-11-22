'use strict';

module.exports = class MessageEvent {
  /**
   * Constructor
   * @param {Object} data
   * @param {Array} ports
   * @param {Object} [source]
   */
  constructor(data, ports, source) {
    this.data = data;
    this.ports = ports;
    this.source = source;
  }
};
