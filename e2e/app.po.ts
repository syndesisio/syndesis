import { by, browser, element, ExpectedConditions, ElementFinder } from 'protractor';
import * as webdriver from 'selenium-webdriver';
import { Promise as P } from 'es6-promise';
import { User, UserDetails } from './common/common';
import { contains } from './common/world';
import { GithubLogin, KeycloakDetails } from './login/login.po';
import { log } from '../src/app/logging';
import * as jQuery from 'jquery';
import WebElement = webdriver.WebElement;


/**
 * Object representation on navigation link (element on left navbar).
 */
class NavLink {
  static readonly selector = 'body > ipaas-root > div > div > ul > li';

  text: string;
  href: string;
  active: boolean;
  element: WebElement;


  public toString = (): string => {
    return `NavLink{${this.text} => ${this.href}, active=${this.active}`;
  }

}

/**
 * Wrapper around session storage inside browser
 */
class SessionStorage {
  /**
   * Get session item of given key from browser
   * @param key session item key
   * @returns {string|null} value of given item
   */
  getItem(key: string): P<string|null> {
    log.info(`Fetching session item '${key}' from browser session storage`);
    // we may need to include  $('ipaas-root').isPresent().then() eventually
    return browser.driver.executeScript((itemKey) => sessionStorage.getItem(itemKey), key);
  }
}

/**
 * Main application with navigation sidebar
 */
export class AppPage {
  static baseurl = '/';
  rootElement = element(by.css('ipaas-root'));
  // rootElement = element(by.css('ipaas-root'));

  sessionStorage = new SessionStorage();

  /**
   * Find links from left navbar
   * @returns {Promise<NavLink>} eventually return list of found links
   */
  findNavLinks(): P<NavLink[]> {

    const elems: P<WebElement[]> = element
      .all(by.css(NavLink.selector))
      .getWebElements();

    return elems.then(found => {
      return found.map(val => {
        const link = new NavLink();
        // using async/await there was problem with getting this attribute
        val.getAttribute('class').then(s => {
          link.active = s.indexOf('active') > -1;
        });

        val.findElement(by.css('a > span.list-group-item-value'))
          .getText().then(text => link.text = text);

        val.findElement(by.css('a'))
          .getAttribute('href').then(href => link.href = href);
        link.element = val;
        return link;
      });
    });


  }

  currentUrl(): P<string> {
    return browser.getCurrentUrl();
  }

  goToUrl(url: string): P<any> {
    return browser.get(url);
  }

  goHome(): P<any> {
    return this.rootElement.element(by.css('a.navbar-brand')).click();
  }

  getButton(buttonTitle: string): ElementFinder {
    log.info(`searching for button ${buttonTitle}`);
    return element(by.buttonText(buttonTitle));
  }

  clickButton(buttonTitle: string): P<any> {
    log.info(`clicking button ${buttonTitle}`);
    return this.getButton(buttonTitle).click();
  }

  getLink(linkTitle: string): ElementFinder {
    log.info(`searching for link ${linkTitle}`);
    return element(by.linkText(linkTitle));
  }

  clickLink(linkTitle: string): P<any> {
    log.info(`clicking link ${linkTitle}`);
    return this.getLink(linkTitle).click();
  }

  getElementByClassName(elementClassName: string): ElementFinder {
    log.info(`searching for element ${elementClassName}`);
    return element(by.className(elementClassName));
  }

  async link(title: String): P<NavLink> {
    const links = await this.findNavLinks();
    return links.filter(l => l.text === title)[0];
  }

  async login(user: User): P<any> {
    // need to disable angular wait before check for current url because we're being redirected outside of angular
    browser.waitForAngularEnabled(false);

    this.goToUrl(AppPage.baseurl);

    // wait either for login page or loaded ipaas app
    await P.race([
      browser.wait(ExpectedConditions.presenceOf(this.rootElement), 1000, 'ipaas root element - assuming we are already logged in'),
      browser.wait(ExpectedConditions.presenceOf(element(by.css('input#login_field'))), 1000, 'Github login page' ).then(() => {
        log.info('GitHub login page');
        return new GithubLogin().login(user);
      }),
    ]);

    await P.race([
      browser.wait(ExpectedConditions.presenceOf(this.rootElement), 1000, 'ipaas root element - assuming we are already logged in'),
      browser.wait(ExpectedConditions.presenceOf(element(by.css('input#firstName'))), 1000, 'Keycloack login page' ).then(() => {
        log.info('Keycloak first time visit page');
        return new KeycloakDetails().submitUserDetails(user.userDetails);
      }),
    ]);

    browser.waitForAngularEnabled(true);
    return this.goToUrl(AppPage.baseurl);
  }


  /**
   * Hook into browser and fetch config.json
   * @returns {any} config.json used in ipaas app
   */
  getSettings(): P<any> {
    // jquery is invoked in the context of the browser
    return browser.driver.executeAsyncScript((callback) => {
      jQuery.get('/config.json', function (data) {
        callback(data);
      });
    }).then(jsonString => JSON.parse(<string> jsonString));
  }

  async getApiUrl(): P<string> {
    const settings = await this.getSettings();
    return settings.apiEndpoint;
  }
}
