const data = require('../data/users.json').users;
import { ElementFinder } from 'protractor';

export class User {
  alias: string;
  description: string;
  username: string;
  password: string;


  constructor(alias: string, description: string) {
    this.alias = alias;
    this.description = description;

    this.username = data[alias].username;
    this.password = data[alias].password;
  }


  toString(): string {
    return `User{alias=${this.alias}, login=${this.username}}`;
  }

}

/**
 * Represents ui component that has it's angular selector.
 */
export interface IPaaSComponent {
  rootElement(): ElementFinder;
}
