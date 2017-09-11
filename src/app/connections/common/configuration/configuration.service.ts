import { Injectable } from '@angular/core';
import { DynamicFormControlModel, DynamicInputModel } from '@ng2-dynamic-forms/core';
import { FormFactoryService } from '../../../common/forms.service';
import { Connection } from '../../../model';

@Injectable()
export class ConnectionConfigurationService {

  constructor(private formFactory: FormFactoryService) { }

  shouldValidate(id: string) {
    switch (id) {
      case 'salesforce':
      case 'twitter':
        return true;
    }
    return false;
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

  getFormModel(connection: Connection, readOnly: boolean): DynamicFormControlModel[] {
    const config = this.getFormConfig(connection);
    const formModel = this.formFactory.createFormModel(config);
    formModel
      .filter(model => model instanceof DynamicInputModel)
      .forEach(model => (<DynamicInputModel>model).readOnly = readOnly);
    return formModel;
  }

  private getFormConfig(connection: Connection) {
    let props = {};
    if (connection.connector) {
      props = this.cloneObject(connection.connector.properties);
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

  cloneObject(obj: {}) {
    return JSON.parse(JSON.stringify(obj));
  }

}
