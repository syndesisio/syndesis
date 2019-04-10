import { Component, Input } from '@angular/core';
import { FormGroup } from '@angular/forms';

import { Connection, Connector } from '@syndesis/ui/platform';
import { ConnectionConfigurationService } from '@syndesis/ui/connections/common/configuration/configuration.service';
import { ConnectorStore } from '@syndesis/ui/store/connector/connector.store';

@Component({
  selector: 'syndesis-connection-configuration-validation',
  templateUrl: './validation.component.html',
  styles: [
    `
    .alert { margin-top: 15px; margin-bottom: 0; }
  `
  ]
})
export class ConnectionConfigurationValidationComponent {
  @Input() connection: Connection;
  @Input() formGroup: FormGroup;
  @Input() placement: 'left' | 'right' = 'left';
  @Input() primaryAction = null;
  validating: boolean;
  validateError: string;
  validateSuccess: boolean;

  constructor(
    private configurationService: ConnectionConfigurationService,
    private connectorStore: ConnectorStore
  ) {}

  showValidateButton(connector: Connector) {
    return this.configurationService.shouldValidate(connector);
  }

  doValidate(connector: Connector, formGroup: FormGroup) {
    this.validateSuccess = false;
    this.validateError = undefined;
    this.validating = true;
    const sanitized = this.configurationService.sanitize(formGroup.value);
    this.connectorStore.validate(connector.id, sanitized).subscribe(
      resp => {
        setTimeout(() => {
          this.validating = false;
          let errorHit = false;
          (<Array<any>>resp).forEach(info => {
            if (!errorHit) {
              if (info['status'] === 'ERROR') {
                errorHit = true;
                this.validateError = (<Array<any>>info)['errors']
                  .map(err => {
                    return err['description'];
                  })
                  .join(', \n');
              }
            }
          });
          if (!errorHit) {
            this.validateSuccess = true;
          }
        }, 10);
      },
      err => {
        setTimeout(() => {
          this.validateError = err.message ? err.message : err;
          this.validating = false;
        }, 10);
      }
    );
  }
}
