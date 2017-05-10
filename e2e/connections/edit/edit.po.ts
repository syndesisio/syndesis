import { SyndesisComponent } from '../../common/common';
import { ElementFinder, $, promise } from 'protractor';
import { P } from '../../common/world';
import { log } from '../../../src/app/logging';
import Promise = promise.Promise;
/**
 * Created by jludvice on 4.4.17.
 */


export class TextEntity {
  readonly selector: ElementFinder;

  constructor(selector: ElementFinder) {
    this.selector = selector;
  }

  get(): P<string> {
    return this.selector.getText();
  }

  set(value: string | P<string>): P<void> {
    return P.resolve(value).then(v => this.selector.sendKeys(v));
  }
}


export class ConnectionViewComponent implements SyndesisComponent {
  name = new TextEntity(this.rootElement().$('input[name="nameInput"]'));
  description = new TextEntity(this.rootElement().$('textarea[name="descriptionInput"]'));

  rootElement(): ElementFinder {
    return $('syndesis-connection-view');
  }

  /**
   * Fill form with given data. It will look for ui element for every map entry.
   * @param data key,value data. Key is used for element lookup.
   * @param parrentElement search inputs in child elements of this one
   * @returns {Promise<[void,T2,T3,T4,T5,T6,T7,T8,T9,T10]>}
   */
  fillForm(data: Map<string, string>, parrentElement: ElementFinder = this.rootElement()): P<void[]> {
    const promises: P<void>[] = [];
    data.forEach((value, key) => {
      log.debug(`filling form item ${key} => ${value}`);
      promises.push(parrentElement.$(`input[name="${key}"`).sendKeys(value));
    });
    return P.all(promises);
  }
}


export class ConnectionCreatePage implements SyndesisComponent {

  rootElement(): ElementFinder {
    return $('syndesis-connection-create-page');
  }
}
