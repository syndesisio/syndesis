import { User, UserDetails } from '../common/common';
import { by, element, browser, ExpectedConditions } from 'protractor';
import { contains, P } from '../common/world';
import { log } from '../../src/app/logging';

/**
 * Created by jludvice on 1.3.17.
 */

export interface LoginPage {
  login(user: User): P<any>;
}

export class GithubLogin implements LoginPage {

  loginSelector = '#login_field';
  passwordSelector = '#password';
  submitSelector = '#login > form > div.auth-form-body.mt-3 > input.btn.btn-primary.btn-block';

  async login(user: User): P<any> {
    const login = element(by.css(this.loginSelector));
    const password = element(by.css(this.passwordSelector));
    const submit = element(by.css(this.submitSelector));

    browser.wait(ExpectedConditions.presenceOf(login), 10000, 'waiting for github login field');
    login.getWebElement().sendKeys(user.username);
    password.getWebElement().sendKeys(user.password);
    await submit.getWebElement().click();

    const url = await browser.getCurrentUrl();
    log.info(`github login, current url: ${url}`);
    if (contains(url, 'https://github.com/login/oauth')) {
      log.info('reauthorizing application');
      // we made too much auth requests, need to reauthorize app
      const reauthorize = element(by.css('#js-oauth-authorize-btn'));
      // wait for reauthorize button to become enabled
      browser.wait(reauthorize.isEnabled, 5000, 'Waiting for reauthorize button to be enabled');
      return reauthorize.getWebElement().click();
    } else {
      return P.resolve();
    }
  }
}

export class KeycloakDetails {

  loginSelector = 'input#username';
  emailSelector = 'input#email';
  firstNameSelector = 'input#firstName';
  lastNameSelector = 'input#lastName';
  submitSelector = '#kc-form-buttons > input.btn.btn-primary.btn-lg';

  async submitUserDetails(userDetails: UserDetails): P<any> {
    const login = element(by.css(this.loginSelector));
    const email = element(by.css(this.emailSelector));
    const firstName = element(by.css(this.firstNameSelector));
    const lastName = element(by.css(this.lastNameSelector));
    const submit = element(by.css(this.submitSelector));

    browser.wait(ExpectedConditions.presenceOf(login), 10000, 'waiting for Keycloak login field');
    email.getWebElement().sendKeys(userDetails.email);
    firstName.getWebElement().sendKeys(userDetails.firstName);
    lastName.getWebElement().sendKeys(userDetails.lastName);
    await submit.getWebElement().click();

    const url = await browser.getCurrentUrl();
    log.info(`Keycloak login, current url: ${url}`);
    return P.resolve();
  }
}
