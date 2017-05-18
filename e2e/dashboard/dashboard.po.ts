import { Promise as P } from 'es6-promise';
import { element, by, ElementFinder } from 'protractor';
import { SyndesisComponent } from '../common/common';
import { log } from '../../src/app/logging';

export class DashboardPage implements SyndesisComponent {
  rootElement(): ElementFinder {
    return element(by.css('syndesis-dashboard'));
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
