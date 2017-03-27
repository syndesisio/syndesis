import { User } from './common';
import { Promise as P } from 'es6-promise';
import { AppPage } from '../app.po';


// create instance of chai and expect instance
// so we can just use import {World, expect} from '../common/world';
export const chai = require('chai').use(require('chai-as-promised'));
export const expect = chai.expect;

// reexport promise type so we don't need to remember to import es6-promise
// we need to export Promise as P because 'Promise' is reserved for async/await usage
export { P };

/**
 * Mimic cucumber.js world.js (couldn't find better alternative)
 * https://github.com/cucumber/cucumber-js/blob/master/docs/support_files/world.md
 *
 */
export class World {
  user: User;
  app: AppPage;

  constructor() {
    this.app = new AppPage();
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

