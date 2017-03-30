import { when, binding, given } from 'cucumber-tsflow';
import { World } from '../common/world';
import { CallbackStepDefinition } from 'cucumber';
import { log } from '../../src/app/logging';

// let http = require('http');

/**
 * Created by jludvice on 29.3.17.
 */


@binding([World])
class ConnectionSteps {

  constructor(private world: World) {
  }


  @when(/^I open url$/)
  public WhenXXX(callback): void {
    // Write code here that turns the phrase above into concrete actions


    const tokenName = 'id_token';

    this.world.app.sessionStorage.getItem(tokenName)
      .then(val => {
        log.info(`${tokenName} => ${val}`);
      })
      .then(() => this.world.app.getApiUrl())
      .then(url => {
        log.info(url);
        callback();
      });
  }
}

export = ConnectionSteps;
