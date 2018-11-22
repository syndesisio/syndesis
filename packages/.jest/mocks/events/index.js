'use strict';

const ExtendableEvent = require('./ExtendableEvent');
const FetchEvent = require('./FetchEvent');
const MessageEvent = require('./MessageEvent');
const NotificationEvent = require('./NotificationEvent');
const PushEvent = require('./PushEvent');

module.exports = {
  create,
  handle,
  mixin,
};

/**
 * Create 'event' instance
 * @param {String} type
 * @returns {ExtendableEvent}
 */
function create(type, ...args) {
  switch (type) {
    case 'fetch':
      return new FetchEvent(...args);
    case 'notificationclick':
      return new NotificationEvent(...args);
    case 'push':
      return new PushEvent(...args);
    case 'message':
      return new MessageEvent(...args);
    default:
      return new ExtendableEvent();
  }
}

/**
 * Handle event 'type' from 'source'
 * @param {Object} source
 * @param {String} type
 * @returns {Promise}
 */
function handle(source, type, ...args) {
  const listeners =
    (source._listeners[type] && source._listeners[type].slice()) || [];
  const onevent = source[`on${type}`];

  if (onevent) {
    listeners.push(onevent);
  }

  if (
    (type === 'error' || type === 'unhandledrejection') &&
    !listeners.length
  ) {
    throw args[0] || Error(`unhandled error of type ${type}`);
  }

  if (listeners.length === 1) {
    return doHandle(listeners[0], type, args);
  }

  return Promise.all(listeners.map(fn => doHandle(fn, type, args)));
}

/**
 * Execute handle of 'listener'
 * @param {Function} listener
 * @param {String} type
 * @param {Array} args
 * @returns {Promise}
 */
function doHandle(listener, type, args) {
  const event = create(type, ...args);

  listener(event);
  return event.promise || Promise.resolve();
}

function mixin(instance) {
  instance._listeners = {};
  instance.addEventListener = function addEventListener(event, fn) {
    if (!instance._listeners[event]) {
      instance._listeners[event] = [];
    }
    instance._listeners[event].push(fn);
  };
  instance.removeEventListener = function removeEventListener(event, fn) {
    if (!instance._listeners[event]) {
      return;
    }
    instance._listeners[event].splice(
      instance._listeners[event].indexOf(fn),
      1
    );
  };
}
