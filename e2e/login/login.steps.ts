import { binding, then } from 'cucumber-tsflow';
import { CallbackStepDefinition } from 'cucumber';
import { ConnectionsListComponent } from '../connections/list/list.po';
import { World, expect } from '../common/world';
import { log } from '../../src/app/logging';


/**
 * Created by jludvice on 1.3.17.
 */
@binding([World])
class LoginSteps {

  constructor(protected world: World) {
  }

  /**
   * this accepts callback and it calls callback once it's done
   * @param connectionCount
   * @param callback
   */
  @then(/^she is presented with at least "(\d+)" connections$/)
  public connectionCount(connectionCount: number, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    log.info(`should assert ${connectionCount}`);

    const page = new ConnectionsListComponent();
    expect(page.countConnections(), `There should be ${connectionCount} available`)
      .to.eventually.be.least(Number(connectionCount)).notify(callback);
  }
}


export = LoginSteps;
