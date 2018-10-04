import { Injectable } from '@angular/core';
import {
  DynamicFormControlModel
} from '@ng-dynamic-forms/core';
import {
  Connection,
  Connector,
  FormFactoryService,
  StringMap,
  ConfigurationProperty
} from '@syndesis/ui/platform';

@Injectable()
export class ConnectionConfigurationService {
  formConfig: StringMap<ConfigurationProperty>;

  constructor(private formFactory: FormFactoryService) {}

  shouldValidate(connector: Connector) {
    const tags = connector ? connector.tags || [] : [];
    return tags.indexOf('verifier') != -1;
  }

  sanitize(data: {}): any {
    return this.formFactory.sanitizeValues(data, this.formConfig);
  }

  getFormModel(connection: Connection): DynamicFormControlModel[] {
    const configAndValues = this.getFormConfig(connection);
    const config = this.formConfig = configAndValues.config;
    const values = configAndValues.values;
    let controls = ['*'];
    // TODO temporary client-side hack to tweak form ordering
    switch (connection.connectorId) {
      case 'activemq':
        controls = [
          'brokerUrl',
          'username',
          'password',
          'clientId',
          'skipCertificateCheck',
          'brokerCertificate',
          'clientCertificate'
        ];
        break;
      default:
    }
    return this.formFactory.createFormModel(config, values, controls);
  }

  cloneObject(obj: any) {
    return JSON.parse(JSON.stringify(obj));
  }

  private getFormConfig(connection: Connection) {
    let config = <StringMap<ConfigurationProperty>>{};
    let values = {};
    if (connection.connector) {
      config = { ...connection.connector.properties };
      values = {
        ...connection.connector.configuredProperties,
        ...connection.configuredProperties
      };
    }
    return { config, values };
  }
}
