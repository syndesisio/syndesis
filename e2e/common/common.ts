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

    const envUsername = process.env[`IPAAS_${alias.toUpperCase()}_USERNAME`] || null;
    const envPassword = process.env[`IPAAS_${alias.toUpperCase()}_PASSWORD`] || null;

    if (envUsername === null || envPassword === null) {
      this.username = data[alias].username;
      this.password = data[alias].password;
    } else {
      this.username = envUsername;
      this.password = envPassword;
    }
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
