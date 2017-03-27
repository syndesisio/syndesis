import * as webdriver from 'selenium-webdriver';
import { Promise as P } from 'es6-promise';
import { element, by, ElementFinder } from 'protractor';
import { IPaaSComponent } from '../../common/common';
import { log } from '../../../src/app/logging';
import WebElement = webdriver.WebElement;
import IWebElementFinders = webdriver.IWebElementFinders;

export class ConnectionsListComponent implements IPaaSComponent {
  rootElement(): ElementFinder {
    return element(by.css('ipaas-connections-list'));
  }

  async countConnections(): P<number> {
    const found = await this.rootElement().all(by.css('a.card-title')).getWebElements();
    log.info(`found ${found.length} connections`);
    return found.length;
  }


  goToConnection(connectionTitle: string): P<any> {
    log.info(`searching for connection ${connectionTitle}`);
    return this.rootElement().element(by.linkText(connectionTitle)).getWebElement().click();
  }
}

export class ConnectionsListPage implements IPaaSComponent {
  rootElement(): ElementFinder {
    return element(by.css('ipaas-connections-list-page'));
  }

  listComponent(): ConnectionsListComponent {
    return new ConnectionsListComponent();
  }
}


