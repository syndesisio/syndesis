import { ElementFinder } from 'protractor';

export class User {
  alias: string;
  description: string;
  username: string;
  password: string;
  userDetails: UserDetails;


  constructor(alias: string, description: string, userDetails: UserDetails) {
    this.alias = alias;
    this.description = description;

    const envUsername = process.env[`SYNDESIS_${alias.toUpperCase()}_USERNAME`] || null;
    const envPassword = process.env[`SYNDESIS_${alias.toUpperCase()}_PASSWORD`] || null;

    if (envUsername === null || envPassword === null) {
      const data = require('../data/users.json').users;
      this.username = data[alias].username;
      this.password = data[alias].password;
    } else {
      this.username = envUsername;
      this.password = envPassword;
    }
    if (userDetails === null) {
      this.userDetails = new UserDetails(`${this.alias}@example.com`, 'FirstName', 'LastName');
    } else {
      this.userDetails = userDetails;
    }
  }


  toString(): string {
    return `User{alias=${this.alias}, login=${this.username}}`;
  }

}

export class UserDetails {
  email: string;
  firstName: string;
  lastName: string;

  constructor(email: string, firstName: string, lastName: string) {
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
  }
}

/**
 * Represents ui component that has it's angular selector.
 */
export interface SyndesisComponent {
  rootElement(): ElementFinder;
}
