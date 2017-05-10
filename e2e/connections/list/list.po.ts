import * as webdriver from 'selenium-webdriver';
import { Promise as P } from 'es6-promise';
import { element, by, ElementFinder } from 'protractor';
import { SyndesisComponent } from '../../common/common';
import { log } from '../../../src/app/logging';
import WebElement = webdriver.WebElement;
import IWebElementFinders = webdriver.IWebElementFinders;

export class ConnectionsListComponent implements SyndesisComponent {
  rootElement(): ElementFinder {
    return element(by.css('syndesis-connections-list'));
  }

  async countConnections(): P<number> {
    const found = await this.rootElement().all(by.css('h2.card-pf-title.text-center')).getWebElements();
    log.info(`found ${found.length} connections`);
    return found.length;
  }


  goToConnection(connectionTitle: string): P<any> {
    log.info(`searching for connection ${connectionTitle}`);
    return this.rootElement().$(`h2.card-pf-title.text-center[title="${connectionTitle}"]`).getWebElement().click();
  }
}

export class ConnectionsListPage implements SyndesisComponent {
  rootElement(): ElementFinder {
    return element(by.css('syndesis-connections-list-page'));
  }

  listComponent(): ConnectionsListComponent {
    return new ConnectionsListComponent();
  }
}
