import { Injectable } from '@angular/core';
import {
  DynamicFormControlModel,
  DynamicInputModel
} from '@ng-dynamic-forms/core';
import { Connection, Connector, FormFactoryService } from '@syndesis/ui/platform';

@Injectable()
export class ConnectionConfigurationService {
  constructor(private formFactory: FormFactoryService) {}

  shouldValidate(connector: Connector) {
    const tags = connector.tags || [];
    return tags.indexOf('verifier') != -1;
  }

  sanitize(data: {}) {
    const sanitized: any = {};
    // strip out any null/empty values
    for (const key of Object.keys(data)) {
      const trimmed = key.trim();
      const value = data[key] || '';
      sanitized[trimmed] = value === '' ? undefined : value;
    }
    return sanitized;
  }

  getFormModel(
    connection: Connection,
    readOnly: boolean
  ): DynamicFormControlModel[] {
    const config = this.getFormConfig(connection);
    let controls = ['*'];
    // TODO temporary client-side hack to tweak form ordering
    switch (connection.connectorId) {
      case 'activemq':
        controls = ['brokerUrl', 'username', 'password', 'clientId', 'skipCertificateCheck', 'brokerCertificate', 'clientCertificate'];
        break;
      case 'aws-s3':
        controls = ['accessKey', 'secretKey', 'region', 'bucketNameOrArn'];
        break;
      default:
    }
    const formModel = this.formFactory.createFormModel(config, undefined, controls);
    formModel
      .filter(model => model instanceof DynamicInputModel)
      .forEach(model => ((<DynamicInputModel>model).readOnly = readOnly));
    return formModel;
  }

  cloneObject(obj: any) {
    return JSON.parse(JSON.stringify(obj));
  }

  private getFormConfig(connection: Connection) {
    let props = {};
    if (connection.connector) {
      props = this.cloneObject(connection.connector.properties);
      if (connection.connector.configuredProperties) {
        Object.keys(connection.connector.configuredProperties).forEach(key => {
          if (props[key]) {
            props[key].value = connection.connector.configuredProperties[key];
          }
        });
      }
      if (connection.configuredProperties) {
        Object.keys(connection.configuredProperties).forEach(key => {
          if (props[key]) {
            props[key].value = connection.configuredProperties[key];
          }
        });
      }
    }
    return props;
  }
}
