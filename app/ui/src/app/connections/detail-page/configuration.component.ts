import {
  Component,
  EventEmitter,
  Input,
  Output,
  OnChanges,
  SimpleChanges
} from '@angular/core';
import { FormGroup } from '@angular/forms';
import { DynamicFormControlModel, DynamicFormService } from '@ng-dynamic-forms/core';

import { Connection } from '@syndesis/ui/platform';
import { ConnectionConfigurationService } from '../common/configuration/configuration.service';

@Component({
  selector: 'syndesis-connection-detail-configuration',
  templateUrl: './configuration.component.html'
})
export class ConnectionDetailConfigurationComponent implements OnChanges {
  @Input() connection: Connection;
  @Output() updated = new EventEmitter<Connection>();
  mode: 'view' | 'edit' = 'view';
  formModel: DynamicFormControlModel[];
  formGroup: FormGroup;

  constructor(
    private configurationService: ConnectionConfigurationService,
    private formService: DynamicFormService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    this.resetView(true);
  }

  edit() {
    this.mode = 'edit';
    this.resetView(false);
  }

  cancel() {
    this.mode = 'view';
    this.resetView(true);
  }

  save() {
    this.mode = 'view';
    this.connection.configuredProperties = this.configurationService.sanitize(
      this.formGroup.value
    );
    this.updated.emit(this.connection);
    this.resetView(true);
  }

  resetView(readOnly: boolean) {
    this.formModel = this.configurationService.getFormModel(
      this.connection,
      readOnly
    );
    this.formGroup = this.formService.createFormGroup(this.formModel);
  }
}
