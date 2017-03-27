import { IPaaSComponent } from '../../common/common';
import { $, by, ElementFinder } from 'protractor';
import { P } from '../../common/world';
import { log } from '../../../src/app/logging';


export class IntegrationsListComponent implements IPaaSComponent {
  rootElement(): ElementFinder {
    return $('ipaas-integrations-list');
  }

  isIntegrationPresent(name: string): P<boolean> {
    log.info(`Checking if integration ${name} is present in the list`);
    return this.rootElement().element(by.cssContainingText('div.name', name)).isPresent();
  }
}


export class IntegrationsListPage implements IPaaSComponent {
  rootElement(): ElementFinder {
    return $('ipaas-integrations-list-page');
  }

  listComponent(): IntegrationsListComponent {
    return new IntegrationsListComponent();
  }
}
