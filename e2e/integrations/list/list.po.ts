import { SyndesisComponent } from '../../common/common';
import { $, by, ElementFinder } from 'protractor';
import { P } from '../../common/world';
import { log } from '../../../src/app/logging';


export class IntegrationsListComponent implements SyndesisComponent {
  rootElement(): ElementFinder {
    return $('syndesis-integrations-list');
  }

  private integrationEntry(name: string): ElementFinder {
    return this.rootElement().element(by.cssContainingText('div.name', name));
  }

  isIntegrationPresent(name: string): P<boolean> {
    log.info(`Checking if integration ${name} is present in the list`);
    return this.integrationEntry(name).isPresent();
  }

  goToIntegration(integrationName: string): P<any> {
    return this.integrationEntry(integrationName).getWebElement().click();
  }

  editIntegration(name: string): P<any> {
    return this.integrationEntry(name).click();
  }
}


export class IntegrationsListPage implements SyndesisComponent {
  rootElement(): ElementFinder {
    return $('syndesis-integrations-list-page');
  }

  listComponent(): IntegrationsListComponent {
    return new IntegrationsListComponent();
  }
}
