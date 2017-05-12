import { SyndesisComponent } from '../../common/common';
import { $, by, ElementFinder } from 'protractor';
import { P } from '../../common/world';
import { log } from '../../../src/app/logging';


export class IntegrationsListComponent implements SyndesisComponent {
  rootElement(): ElementFinder {
    return $('syndesis-integrations-list');
  }

  isIntegrationPresent(name: string): P<boolean> {
    log.info(`Checking if integration ${name} is present in the list`);
    return this.rootElement().element(by.cssContainingText('div.name', name)).isPresent();
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
