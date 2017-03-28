import { IPaaSComponent } from '../../common/common';
import { element, by, ElementFinder } from 'protractor';
import { P } from '../../common/world';
import { ConnectionsListComponent } from '../../connections/list/list.po';
import { log } from '../../../src/app/logging';


export class FlowConnection {

  constructor(public type: string, public element: ElementFinder) {
  }

  /**
   * Check if this element is active
   * @returns {webdriver.promise.Promise<boolean>}
   */
  isActive(): P<boolean> {
    return this.element.element(by.css('p.icon.active')).isPresent();
  }

}

export class FlowViewComponent implements IPaaSComponent {
  static readonly nameSelector = 'input.form-control.integration-name';

  rootElement(): ElementFinder {
    return element(by.css('ipaas-integrations-flow-view'));
  }

  getIntegrationName(): P<string> {
    return this.rootElement().element(by.css(FlowViewComponent.nameSelector)).getText();
  }

  /**
   * Get div
   * @param type (start|finish)
   */
  async flowConnection(type: string): P<FlowConnection> {
    type = type.toLowerCase();
    const e = await this.rootElement().element(by.css(`div.row.steps.${type}`));
    return new FlowConnection(type, e);
  }
}

export class ListActionsComponent implements IPaaSComponent {
  rootElement(): ElementFinder {
    return element(by.css('ipaas-list-actions'));
  }

  selectAction(name: string): P<any> {
    log.info(`searching for integration action '${name}'`);
    return this.rootElement().element(by.cssContainingText('div.name', name)).click();
  }

}

export class ConnectionSelectComponent implements IPaaSComponent {
  rootElement(): ElementFinder {
    return element(by.css('ipaas-integrations-connection-select'));
  }

  connectionListComponent(): ConnectionsListComponent {
    return new ConnectionsListComponent();
  }


}


export class IntegrationBasicsComponent implements IPaaSComponent {
  static readonly nameSelector = 'input[name="nameInput"]';
  static readonly descriptionSelector = 'textarea[name="descriptionInput"]';

  rootElement(): ElementFinder {
    return element(by.css('ipaas-integrations-integration-basics'));
  }

  setName(name: string): P<any> {
    log.debug(`setting integration nae to ${name}`);
    return this.rootElement().$(IntegrationBasicsComponent.nameSelector).sendKeys(name);
  }

  setDescription(description: string): P<any> {
    return this.rootElement().$(IntegrationBasicsComponent.descriptionSelector).sendKeys(description);
  }


}

export class IntegrationEditPage implements IPaaSComponent {

  rootElement(): ElementFinder {
    return element(by.css('ipaas-integrations-edit-page'));
  }

  flowViewComponent(): FlowViewComponent {
    return new FlowViewComponent();
  }

  connectionSelectComponent(): ConnectionSelectComponent {
    return new ConnectionSelectComponent();
  }

  basicsComponent(): IntegrationBasicsComponent {
    return new IntegrationBasicsComponent();
  }


}


