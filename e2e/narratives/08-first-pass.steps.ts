/**
 * Created by jludvice on 8.3.17.
 */
import { binding, given, when, then } from 'cucumber-tsflow';
import { TableDefinition, CallbackStepDefinition } from 'cucumber';
import { World, P, expect } from '../common/world';
import { ConnectionsListPage, ConnectionsListComponent } from '../connections/list/list.po';
import { ConnectionDetailPage } from '../connections/detail/detail.po';
import { IntegrationEditPage } from '../integrations/edit/edit.po';

/**
 * Created by jludvice on 1.3.17.
 */
@binding([World])
class FirstPass {

  //todo consider using inheriance in *.steps.ts
  constructor(protected world: World) {
  }


  @given(/^details for "([^"]*)" connection:$/)
  public setConnectionDetails(arg1: string, table: TableDefinition, callback: CallbackStepDefinition): void {

    console.log(`should set connection details for ${arg1}: ${table}`);
    callback();
  }

  @then(/^Camilla is presented with the iPaaS homepage "([^"]*)"$/)
  public async verifyHomepage(arg1: string): P<any> {
    // Write code here that turns the phrase above into concrete actions
    const currentLink = await this.world.app.link(arg1);
    console.log(currentLink);
    expect(currentLink.active, 'Dashboard link must be active').to.be.true;
  }


  @when(/^Camilla selects an existing "([^"]*)" connection to view the configuration details for that connection\.$/)
  public select(arg1: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions

    const page = new ConnectionsListComponent();
    page.goToConnection(arg1).then((res) => {
      callback();
    });
  }

  @then(/^Camilla is presented with "([^"]*)" connection details$/)
  public verifyConnectionDetails(connectionName: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    const page = new ConnectionDetailPage();
    expect(page.connectionName(), `Connection detail page must show connection name`)
      .to.eventually.be.equal(connectionName).notify(callback);
    // todo add more assertion on connection details page
  }


  @when(/^Camilla navigates to the iPaaS "([^"]*)"$/)
  public navigateIpaas(arg1: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    callback(null, 'pending');
  }


  @when(/^clicks on a "([^"]*)" button to create a new integration\.$/)
  public clickOnButton(buttonTitle: string, callback: CallbackStepDefinition): void {
    this.world.app.clickButton(buttonTitle)
      .then(() => callback())
      // it may fail but we still want to let tests continue
      .catch((e) => callback(e));

  }


  @then(/^she is presented with a visual integration editor$/)
  public editorOpened(callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    const page = new IntegrationEditPage();
    expect(page.rootElement().isPresent(), 'there must be edit page root element')
      .to.eventually.be.true.notify(callback);
  }


  /**
   * whether it's start or finish connection
   * @param type
   * @param callback
   */
  @then(/^she is prompted to select a "([^"]*)" connection from a list of available connections$/)
  public async verifyTypeOfConnection(type: string): P<any> {
    // Write code here that turns the phrase above into concrete actions

    const page = new IntegrationEditPage();

    const connection = await page.flowViewComponent().flowConnection(type);

    return expect(connection.isActive(), `${type} connection must be active`)
      .to.eventually.be.true;
  }


  @when(/^Camilla selects the "([^"]*)" connection.*$/)
  public selectConnection(connectionName: string): P<any> {
    // Write code here that turns the phrase above into concrete actions
    const page = new IntegrationEditPage();
    return page.connectionSelectComponent().connectionListComponent().goToConnection(connectionName);
  }


  @when(/^she fills "([^"]*)" connection details$/)
  public fillConnectionDetails(connectionName: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    callback(null, 'pending');
  }


  @when(/^click "([^"]*)" button$/)
  public clickButton(title: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    callback(null, 'pending');
  }


  @when(/^Camilla navigates to the "([^"]*)" page$/)
  public navigateToPage(pageTitle: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    callback(null, 'pending');
  }


  @when(/^selects the "([^"]*)" integration$/)
  public selectIntegration(integrationTitle: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    callback(null, 'pending');
  }


  @then(/^she is presented with "([^"]*)" integration detail$/)
  public verifyIntegrationDetail(arg1: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    callback(null, 'pending');
  }
}

export = FirstPass;
