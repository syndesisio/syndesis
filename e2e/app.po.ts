import { by, browser, element, ExpectedConditions, ElementFinder } from 'protractor';
import * as webdriver from 'selenium-webdriver';
import { Promise as P } from 'es6-promise';
import { User, UserDetails } from './common/common';
import { contains } from './common/world';
import { GithubLogin, KeycloakDetails, OpenShiftAuthorize } from './login/login.po';
import { log } from '../src/app/logging';
import * as jQuery from 'jquery';
import WebElement = webdriver.WebElement;


/**
 * Object representation on navigation link (element on left navbar).
 */
class NavLink {
  static readonly selector = 'body > syndesis-root > div > div > ul > li';

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
    // we may need to include  $('syndesis-root').isPresent().then() eventually
    return browser.driver.executeScript((itemKey) => sessionStorage.getItem(itemKey), key);
  }
}

/**
 * Main application with navigation sidebar
 */
export class AppPage {
  static baseurl = '/';
  rootElement = element(by.css('syndesis-root'));
  // rootElement = element(by.css('syndesis-root'));

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

  getTitleByText(text: string): ElementFinder {
    log.info(`searching for title ${text}`);
    return element(by.cssContainingText('h2', text));
  }

  clickDeleteIntegration(integrationName: string, rootElement: ElementFinder): P<any> {
    log.info(`clicking delete link for integration ${integrationName}`);
    log.info(`root element ${rootElement}`);

    const parentElement = rootElement.all(by.className('integration')).filter(function(elem, index) {
      return elem.element(by.className('name')).getText().then(function(text) {
        return text === integrationName;
      });
    });

    parentElement.first().element(by.id('dropdownKebabRight9')).click();
    this.rootElement.element(by.linkText('Delete')).click();
    return this.rootElement.element(by.buttonText('Delete')).click();
  }

  async link(title: String): P<NavLink> {
    const links = await this.findNavLinks();
    return links.filter(l => l.text === title)[0];
  }

  async login(user: User): P<any> {
    // need to disable angular wait before check for current url because we're being redirected outside of angular
    browser.waitForAngularEnabled(false);
    await this.goToUrl(AppPage.baseurl);

    let currentUrl = await browser.getCurrentUrl();
    const isAppLoaded = await this.rootElement.element(by.css('span.username')).isPresent();
    if (contains(currentUrl, 'github.com/login') || !isAppLoaded) {
      log.info('GitHub login page');
      await new GithubLogin().login(user);
    }
    currentUrl = await browser.getCurrentUrl();
    if (contains(currentUrl, 'oauth/authorize/approve')) {
      log.info('Authorize access login page');
      await new OpenShiftAuthorize().authorizeAccess();
    }
    currentUrl = await browser.getCurrentUrl();
    if (contains(currentUrl, 'auth/realms')) {
      log.info('Keycloak login page');
      await new KeycloakDetails().submitUserDetails(user.userDetails);
    }
    //We get authorize app request upon first clean login
    currentUrl = await browser.getCurrentUrl();
    if (contains(currentUrl, 'github.com/login/oauth')) {
      log.info('Second GitHub AuthPage page');
      await new GithubLogin().authorizeApp();
    }

    await browser.wait(ExpectedConditions.presenceOf(this.rootElement), 30 * 1000,
    'syndesis root element - assuming we are already logged in');

    browser.waitForAngularEnabled(true);
    return this.goToUrl(AppPage.baseurl);
  }


  /**
   * Hook into browser and fetch config.json
   * @returns {any} config.json used in syndesis app
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
