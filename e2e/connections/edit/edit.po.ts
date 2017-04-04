import { IPaaSComponent } from '../../common/common';
import { ElementFinder, $, promise } from 'protractor';
import { P } from '../../common/world';
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


export class ConnectionViewComponent implements IPaaSComponent {
  name = new TextEntity(this.rootElement().$('input[name="nameInput"]'));
  description = new TextEntity(this.rootElement().$('textarea[name="descriptionInput"]'));

  rootElement(): ElementFinder {
    return $('ipaas-connection-view');
  }
}


export class ConnectionCreatePage implements IPaaSComponent {

  rootElement(): ElementFinder {
    return $('ipaas-connection-create-page');
  }
}
