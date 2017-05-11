import * as webdriver from 'selenium-webdriver';
import { Promise as P } from 'es6-promise';
import { element, by, ElementFinder } from 'protractor';
import { IPaaSComponent } from '../common/common';
import { log } from '../../src/app/logging';
import { AppPage } from '../app.po';
import WebElement = webdriver.WebElement;
import IWebElementFinders = webdriver.IWebElementFinders;

export class DashboardPage implements IPaaSComponent {
  rootElement(): ElementFinder {
    return element(by.css('ipaas-dashboard'));
  }

  getConnection(connectionTitle: string): ElementFinder {
    log.info(`searching for connection ${connectionTitle}`);
    return this.rootElement().$(`h2.card-pf-title.text-center[title="${connectionTitle}"]`);
  }

  goToConnection(connectionTitle: string): P<any> {
    log.info(`searching for connection ${connectionTitle}`);
    return this.rootElement().$(`h2.card-pf-title.text-center[title="${connectionTitle}"]`).getWebElement().click();
  }

  isIntegrationPresent(integrationName: string): P<boolean> {
    log.info(`Checking if integration ${integrationName} is present in the list`);
    return this.rootElement().element(by.cssContainingText('div.name', integrationName)).isPresent();
  }
}
