'use strict';

const ExtendableEvent = require('./ExtendableEvent');

module.exports = class PushEvent extends ExtendableEvent {
  /**
   * Constructor
   * @param {Object} data
   */
  constructor(data) {
    super();
    this.data = data;
  }
};
