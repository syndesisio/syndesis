/**
 * Created by jludvice on 8.3.17.
 */
import { CallbackStepDefinition } from 'cucumber';
import { binding, given, when, then } from 'cucumber-tsflow';
import { Promise as P } from 'es6-promise';
import { World, expect } from './world';
import { User } from './common';
import { log } from '../../src/app/logging';
/**
 * Generic steps that can be used in various features
 * They may change state through world class.
 * See https://github.com/timjroberts/cucumber-js-tsflow#sharing-data-between-bindings
 */
@binding([World])
class CommonSteps {

  constructor(protected world: World) {
  }

  @given(/^credentials for "([^"]*)"$/)
  public loadCredentials(alias: string, callback: CallbackStepDefinition): void {
    this.world.user = new User(alias, 'asdfadf');

    callback();
  }

  @when(/^"(\w+)" logs into the iPaaS.*$/i)
  public login(alias: string): P<any> {
    this.world.user = new User(alias.toLowerCase(), 'asdfadf');
    // return this.app.login(this.world.user);
    return this.world.app.login(this.world.user);
  }


  /**
   * This method uses async/await and returns promise once it's done
   * @param linkTitle
   * @returns {Promise<P<any>>}
   */
  @when(/^"(\w+)" navigates? to the "([^"]*)" page.*$/)
  public async goToNavLink(alias: string, linkTitle: string): P<any> {
    // const link = await this.app.link(linkTitle);
    log.info(`navigating ${alias} to ${linkTitle} page`);
    if (linkTitle === 'Home') {
      return this.world.app.goHome();
    }
    const link = await this.world.app.link(linkTitle);
    expect(link, `Navigation link ${linkTitle} should exist`).to.exist;
    return link.element.click();
  }

  @then(/^(\w+)? ?is presented with the iPaaS page "([^"]*)"$/)
  public async verifyHomepage(alias: string, pageTitle: string): P<any> {
    // Write code here that turns the phrase above into concrete actions
    const currentLink = await this.world.app.link(pageTitle);
    log.info(`${alias} is on current navlink: ${currentLink}`);
    expect(currentLink.active, `${pageTitle} link must be active`).to.be.true;
  }
    
  @then(/^(\w+)? ?is presented with the "([^"]*)" link$/)
  public async verifyLink(alias: string, linkTitle: string): P<any> {
    const currentLink = await this.world.app.getLink(linkTitle);

    expect(currentLink.isPresent(), `There must be present a link ${linkTitle}`)
      .to.eventually.be.true;
  }  

  @when(/clicks? on the "([^"]*)" button.*$/)
  public clickOnButton(buttonTitle: string, callback: CallbackStepDefinition): void {
    this.world.app.clickButton(buttonTitle)
      .then(() => callback())
      // it may fail but we still want to let tests continue
      .catch((e) => callback(e));
  }

  @when(/clicks? on the "([^"]*)" link.*$/)
  public clickOnLink(linkTitle: string, callback: CallbackStepDefinition): void {
    this.world.app.clickLink(linkTitle)
      .then(() => callback())
      // it may fail but we still want to let tests continue
      .catch((e) => callback(e));
  }
  
  @then(/^she is presented with the "([^"]*)" button.*$/)
  public expectButtonPresent(buttonTitle: string, callback: CallbackStepDefinition): void {

    const button = this.world.app.getButton(buttonTitle);
    expect(button.isPresent(), `There must be present a button ${buttonTitle}`)
      .to.eventually.be.true;

    expect(button.isPresent(), `There must be enabled button ${buttonTitle}`)
      .to.eventually.be.true.notify(callback);
  }
}

export = CommonSteps;
