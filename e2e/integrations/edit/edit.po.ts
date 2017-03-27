import { IPaaSComponent } from '../../common/common';
import { element, by, ElementFinder } from 'protractor';
import { P } from '../../common/world';
import { ConnectionsListComponent } from '../../connections/list/list.po';


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
  rootElement(): ElementFinder {
    return element(by.css('ipaas-integrations-flow-view'));
  }

  getIntegrationName(): P<string> {
    return this.rootElement().element(by.css('#integrationName')).getText();
  }

  setIntegrationName(name: string): P<any> {
    return this.rootElement().element(by.css('#integrationName')).sendKeys(name);
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

export class ConnectionSelectComponent implements IPaaSComponent {
  rootElement(): ElementFinder {
    return element(by.css('ipaas-integrations-connection-select'));
  }

  connectionListComponent(): ConnectionsListComponent {
    return new ConnectionsListComponent();
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


}


