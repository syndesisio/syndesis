/**
 * Created by jludvice on 8.3.17.
 */
import { CallbackStepDefinition, TableDefinition } from 'cucumber';
import { browser } from 'protractor';
import { binding, given, then, when } from 'cucumber-tsflow';
import { Promise as P } from 'es6-promise';
import { expect, World } from './world';
import { User, UserDetails } from './common';
import { log } from '../../src/app/logging';
import { IntegrationsListPage, IntegrationsListComponent } from '../integrations/list/list.po';
import { DashboardPage } from '../dashboard/dashboard.po';
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
    this.world.user = new User(alias.toLowerCase(), 'asdfadf', null);
    log.info(`using alias ${alias} with login ${this.world.user.username}`);
    callback();
  }

  @when(/^"(\w+)" logs into the Syndesis.*$/i)
  public login(alias: string): P<any> {
    this.world.user = new User(alias.toLowerCase(), 'asdfadf', null);
    // return this.app.login(this.world.user);
    return this.world.app.login(this.world.user);
  }

  @given(/^clean application state$/)
  public async resetState(): P<any> {
    // user must be logged in (we need his token)
    const result = await this.world.app.login(this.world.user);
    // reset state or fail this step
    return this.world.resetState();
  }

  @given(/^application state "([^"]*)"$/)
  public async setState(jsonName: string): P<any> {
    // user must be logged in (we need his token)
    const result = await this.world.app.login(this.world.user);
    // reset state or fail this step
    return this.world.setState(jsonName);
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

  @then(/^(\w+)? ?is presented with the Syndesis page "([^"]*)"$/)
  public async verifyHomepage(alias: string, pageTitle: string): P<any> {
    // Write code here that turns the phrase above into concrete actions
    const currentLink = await this.world.app.link(pageTitle);
    log.info(`${alias} is on current navlink: ${currentLink}`);
    expect(currentLink.active, `${pageTitle} link must be active`).to.be.true;
  }

  @then(/^(\w+)? ?is presented with the "([^"]*)" link*$/)
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

  @then(/^she is presented with the "([^"]*)" tables*$/)
  public expectTableTitlesPresent(tableTitles: string, callback: CallbackStepDefinition): void {

    const tableTitlesArray = tableTitles.split(', ');

    for (const tableTitle of tableTitlesArray) {
      this.expectTableTitlePresent(tableTitle, callback);
    }
  }

  public expectTableTitlePresent(tableTitle: string, callback: CallbackStepDefinition): void {

    const table = this.world.app.getTitleByText(tableTitle);
    expect(table.isPresent(), `There must be present a table ${tableTitle}`)
      .to.eventually.be.true;

    expect(table.isPresent(), `There must be enabled table ${tableTitle}`)
      .to.eventually.be.true.notify(callback);
  }

  @then(/^she is presented with the "([^"]*)" elements*$/)
  public expectElementsPresent(elementClassNames: string, callback: CallbackStepDefinition): void {

    const elementClassNamesArray = elementClassNames.split(',');

    for (const elementClassName of elementClassNamesArray) {
      this.expectElementPresent(elementClassName, callback);
    }
  }

  @then(/^she is presented with the "([^"]*)"$/)
  public expectElementPresent(elementClassName: string, callback: CallbackStepDefinition): void {

    const element = this.world.app.getElementByClassName(elementClassName);
    expect(element.isPresent(), `There must be present a element ${elementClassName}`)
      .to.eventually.be.true;

    expect(element.isPresent(), `There must be enabled element ${elementClassName}`)
      .to.eventually.be.true.notify(callback);
  }

  @then(/^Integration "([^"]*)" is present in top 5 integrations$/)
  public expectIntegrationPresentinTopFive(name: string, callback: CallbackStepDefinition): void {
    log.info(`Verifying integration ${name} is present in top 5 integrations`);
    const page = new DashboardPage();
    expect(page.isIntegrationPresent(name), `Integration ${name} must be present`)
      .to.eventually.be.true.notify(callback);
  }

  @then(/^Camilla can see "([^"]*)" connection on dashboard page$/)
  public expectConnectionTitlePresent (connectionName: string, callback: CallbackStepDefinition): void {
    const dashboard = new DashboardPage();
    const connection = dashboard.getConnection(connectionName);
    expect(connection.isPresent(), `There should be present connection ${connectionName}`)
      .to.eventually.be.true.notify(callback);
  }

  @then(/^Camilla can not see "([^"]*)" connection on dashboard page anymore$/)
  public expectConnectionTitleNonPresent (connectionName: string, callback: CallbackStepDefinition): void {
    const dashboard = new DashboardPage();
    const connection = dashboard.getConnection(connectionName);
    expect(connection.isPresent(), `There shouldnt be a present connection ${connectionName}`)
      .to.eventually.be.false.notify(callback);
  }

  @when(/^Camilla deletes the "([^"]*)" integration*$/)
  public deleteIntegration(integrationName: string): P<any> {
    const listComponent = new IntegrationsListComponent();
    return this.world.app.clickDeleteIntegration(integrationName, listComponent.rootElement());
  }

  @then(/^Camilla can not see "([^"]*)" integration anymore$/)
  public expectIntegrationPresent(name: string, callback: CallbackStepDefinition): void {
    log.info(`Verifying if integration ${name} is present`);
    const page = new IntegrationsListPage();
    expect(page.listComponent().isIntegrationPresent(name), `Integration ${name} must be present`)
      .to.eventually.be.false.notify(callback);
  }

  @when(/^Camilla deletes the "([^"]*)" integration in top 5 integrations$/)
  public deleteIntegrationOnDashboard(integrationName: string): P<any> {
    log.info(`Trying to delete ${integrationName} on top 5 integrations table`);
    const dashboard = new DashboardPage();
    return this.world.app.clickDeleteIntegration(integrationName, dashboard.rootElement());
  }

  @then(/^Camilla can not see "([^"]*)" integration in top 5 integrations anymore$/)
  public expectIntegrationPresentOnDashboard(name: string, callback: CallbackStepDefinition): void {
    log.info(`Verifying if integration ${name} is present`);
    const dashboard = new DashboardPage();
    expect(dashboard.isIntegrationPresent(name), `Integration ${name} must be present`)
      .to.eventually.be.false.notify(callback);
  }

  /**
   * Scroll the webpage.
   *
   * @param topBottom possible values: top, bottom
   * @param leftRight possible values: left, right
   * @returns {Promise<any>}
   */
  @when(/^scroll "([^"]*)" "([^"]*)"$/)
  public async scroll(topBottom: string, leftRight: string): P<any> {

    const size = await browser.manage().window().getSize();
    const directions: Object = {
      top: 0,
      bottom: size.height,
      left: 0,
      right: size.width,
    };
    if (!directions.hasOwnProperty(topBottom) || !directions.hasOwnProperty(leftRight)) {
      return P.reject(`unknown directions [${topBottom}, ${leftRight}`);
    }
    const x = directions[leftRight];
    const y = directions[topBottom];

    log.info(`scrolling to [x=${x},y=${y}]`);
    return browser.driver.executeScript((browserX, browserY) => window.scrollTo(browserX, browserY), x, y);
  }
}

export = CommonSteps;
