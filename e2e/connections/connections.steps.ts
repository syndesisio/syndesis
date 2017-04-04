import { when, binding, then } from 'cucumber-tsflow';
import { World, expect, P } from '../common/world';
import { CallbackStepDefinition } from 'cucumber';
import { ConnectionDetailPage } from './detail/detail.po';
import { ConnectionsListComponent } from './list/list.po';
import { ConnectionViewComponent } from './edit/edit.po';

// let http = require('http');

/**
 * Created by jludvice on 29.3.17.
 */


@binding([World])
class ConnectionSteps {

  constructor(private world: World) {
  }

  @then(/^Camilla is presented with "([^"]*)" connection details$/)
  public verifyConnectionDetails(connectionName: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    const page = new ConnectionDetailPage();
    expect(page.connectionName(), `Connection detail page must show connection name`)
      .to.eventually.be.equal(connectionName).notify(callback);
    // todo add more assertion on connection details page
  }

  @when(/^Camilla selects the "([^"]*)" connection.*$/)
  public selectConnection(connectionName: string): P<any> {
    // Write code here that turns the phrase above into concrete actions
    const listComponent = new ConnectionsListComponent();
    return listComponent.goToConnection(connectionName);
  }

  @when(/^type "([^"]*)" into connection name$/)
  public typeConnectionName(name: string): P<void> {
    // Write code here that turns the phrase above into concrete actions
    const connectionView = new ConnectionViewComponent();
    return connectionView.name.set(name);
  }

  @when(/^type "([^"]*)" into connection description/)
  public typeConnectionDescription(description: string): P<void> {
    // Write code here that turns the phrase above into concrete actions
    const connectionView = new ConnectionViewComponent();
    return connectionView.description.set(description);
  }
}

export = ConnectionSteps;
