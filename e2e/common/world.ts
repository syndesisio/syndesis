import { User } from './common';
import { Promise as P } from 'es6-promise';
import { AppPage } from '../app.po';
import { log } from '../../src/app/logging';
import https = require('https');
import http = require('http');
import url = require('url');
import request = require('request');
import fs = require('fs-extra');


// let r  = require('request');
// create instance of chai and expect instance
// so we can just use import {World, expect} from '../common/world';
export const chai = require('chai').use(require('chai-as-promised'));
export const expect = chai.expect;

// reexport promise type so we don't need to remember to import es6-promise
// we need to export Promise as P because 'Promise' is reserved for async/await usage
export { P };

export const OAUTH_TOKEN_NAME = 'access_token';

/**
 * Wrap http status code and response data into single object.
 */
export interface SyndesisResult {
  statusCode: number;
  data: any;
}

/**
 * Helper for easier wrapping of callbacks into Promise.
 */
export class Deferred<T> {

  promise: P<T>;
  resolve: (value?: T | PromiseLike<T>) => void;
  reject: (reason?: any) => void;

  constructor() {
    this.promise = new P<T>((resolve, reject) => {
      this.resolve = resolve;
      this.reject = reject;
    });
  }
}

/**
 * Mimic cucumber.js world.js (couldn't find better alternative)
 * https://github.com/cucumber/cucumber-js/blob/master/docs/support_files/world.md
 *
 */
export class World {
  user: User;
  app: AppPage;
  private token: string = null;
  connectionDetails: Map<string, Map<string, string>> = new Map();

  constructor() {
    this.app = new AppPage();
  }


  /**
   * Fetch oauth token from application session store or return cached one.
   * @returns {Promise<string>}
   */
  async getOauthToken(): P<string> {
    if (this.token == null) {
      this.token = await this.app.sessionStorage.getItem(OAUTH_TOKEN_NAME);
    }
    return this.token;
  }


  async authRequest(urlString: string, method: string = 'GET', data: string = null, encoding: string = null): P<SyndesisResult> {
    const link = url.parse(urlString);
    const deferred: Deferred<SyndesisResult> = new Deferred();

    const token = await this.getOauthToken();
    const options: http.RequestOptions = {
      method: method,
      protocol: link.protocol,
      hostname: link.hostname,
      path: link.path,
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    };

    log.info(`invoking http request with options: ${JSON.stringify(options)}`);
    const req = https.request(options, (res) => {
      let receivedData = '';

      res.on('data', chunk => {
        log.debug(`data chunk (${Object.prototype.toString.call(chunk)}): ${chunk}`);
        receivedData += chunk;
      });

      res.on('end', () => {
        log.info(`http request finished with status: ${res.statusCode}`);
        const result: SyndesisResult = {
          statusCode: res.statusCode,
          data: receivedData,
        };
        deferred.resolve(result);
      });

    });

    if (data !== null) {
      log.info(`writing data buffer to request`);
      req.write(data);
    }
    req.end();
    return deferred.promise;
  }

  async resetState(): P<boolean> {
    const link = await this.app.getApiUrl();
    log.info('resetting application state');

    const result = await this.authRequest(`${link}/test-support/reset-db`);
    if (result.statusCode !== 204) {
      throw new Error(`Failed to reset state. Response: '${JSON.stringify(result)}`);
    }
    return true;
  }

  async setState(jsonName: string): P<any> {
    const link = await this.app.getApiUrl();

    const jsonPath = `e2e/data/${jsonName}`;
    log.info(`resetting state from file ${jsonPath}`);
    if (!fs.existsSync(jsonPath)) {
      return P.reject(`File path ${jsonPath} doesn't exist`);
    }

    const data: string = fs.readFileSync(jsonPath);
    log.debug(`data: ${data}`);
    const result = await this.authRequest(`${link}/test-support/restore-db`, 'POST', data);
    if (result.statusCode !== 204) {
      throw new Error(`Failed to set state. Response: '${JSON.stringify(result)}`);
    }
    return true;
  }

  async snapshot(): P<any> {
    const link = await this.app.getApiUrl();

    const result = await this.authRequest(`${link}/test-support/snapshot-db`);
    if (result.statusCode !== 200) {
      throw new Error(`Failed to get snapshot ${JSON.stringify(result)}`);
    }
    return JSON.parse(result.data);
  }
}


// helpers (move them to it's own file eventually)
export function contains(text: string, substring: string): boolean {
  return text.indexOf(substring) > -1;
}

export function filterAsync(array: any[], filter: (any) => P<boolean>): P<any[]> {
  return P.all(array.map(entry => filter(entry)))
    .then(bits => array.filter(entry => bits.shift()));
}

