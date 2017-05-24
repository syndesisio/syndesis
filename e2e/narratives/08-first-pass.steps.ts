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


  @when(/^Camilla navigates to Syndesis "([^"]*)"$/)
  public navigateSyndesis(arg1: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    callback(null, 'pending');
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
}

export = FirstPass;
