'use strict';

const MessagePort = require('./MessagePort');

module.exports = class MessageChannel {
  constructor() {
    this.port1 = new MessagePort();
    this.port2 = new MessagePort(this.port1);
  }
};
