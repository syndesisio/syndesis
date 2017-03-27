import { by, browser, element, ExpectedConditions } from 'protractor';
import * as webdriver from 'selenium-webdriver';
import { Promise as P } from 'es6-promise';
import { User } from './common/common';
import { contains } from './common/world';
import { GithubLogin } from './login/login.po';
import { log } from '../src/app/logging';
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
 * Main application with navigation sidebar
 */
export class AppPage {
  static baseurl = '/';
  rootElement = element(by.css('ipaas-root'));
  // rootElement = element(by.css('ipaas-root'));

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

  clickButton(buttonTitle: string): P<any> {
    log.info(`clicking button ${buttonTitle}`);
    const button = element(by.buttonText(buttonTitle));

    // return expect(button.isPresent(), `button ${buttonTitle} must be present`).to.eventually.be.true
    //   .then(()=> button.click())
    return element(by.buttonText(buttonTitle)).click();
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
      browser.wait(ExpectedConditions.presenceOf(element(by.css('input'))), 1000, 'Some input field - assuming we are on login page'),
    ]);

    const url = await this.currentUrl();

    if (contains(url, 'github')) {
      // we need to login on github
      await new GithubLogin().login(user);
    } else if (contains(url, browser.baseUrl)) {
      // pass - we're already logged in
    } else {
      return P.reject(`Unsupported login page ${url}`);
    }
    browser.waitForAngularEnabled(true);
    return this.goToUrl(AppPage.baseurl);
  }
}
