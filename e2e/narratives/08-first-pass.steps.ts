/**
 * Created by jludvice on 8.3.17.
 */
import { binding, given, when, then } from 'cucumber-tsflow';
import { TableDefinition, CallbackStepDefinition } from 'cucumber';
import { World, P, expect } from '../common/world';
import { ConnectionsListComponent } from '../connections/list/list.po';
import { ConnectionDetailPage } from '../connections/detail/detail.po';
import { IntegrationEditPage, ListActionsComponent } from '../integrations/edit/edit.po';
import { log } from '../../src/app/logging';
import { IntegrationsListPage } from '../integrations/list/list.po';

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

    log.info(`should set connection details for ${arg1}: ${table}`);
    callback();
  }


  @when(/^Camilla navigates to the iPaaS "([^"]*)"$/)
  public navigateIpaas(arg1: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    callback(null, 'pending');
  }

  @when(/defines integration name "([^"]*)"$/)
  public defineIntegrationName(integrationName: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    const page = new IntegrationEditPage();

    page.basicsComponent().setName(integrationName).then(() => callback());
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


  @when(/^she selects "([^"]*)" integration action$/)
  public selectIntegrationAction(action: string, callback): void {
    const page = new ListActionsComponent();
    page.selectAction(action).then(() => callback());
  }


  @when(/^she fills "([^"]*)" connection details$/)
  public fillConnectionDetails(connectionName: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    callback(null, 'pending');
  }


  @then(/^Integration "([^"]*)" is present in integrations list$/)
  public expectIntegrationPresent(name: string, callback: CallbackStepDefinition): void {
    log.info(`Verifying integration ${name} is present`);
    const page = new IntegrationsListPage();
    expect(page.listComponent().isIntegrationPresent(name), `Integration ${name} must be present`)
      .to.eventually.be.true.notify(callback);
  }
}

export = FirstPass;
