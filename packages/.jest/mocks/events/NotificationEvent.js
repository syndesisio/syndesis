'use strict';

const ExtendableEvent = require('./ExtendableEvent');

module.exports = class NotificationEvent extends ExtendableEvent {
  /**
   * Constructor
   * @param {Notification} notification
   */
  constructor(notification) {
    super();
    this.notification = notification;
  }
};
