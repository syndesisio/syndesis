import {
  Component,
  EventEmitter,
  Input,
  Output,
  OnChanges,
  SimpleChanges,
  OnInit
} from '@angular/core';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService
} from '@ng-dynamic-forms/core';

import { Connection, ApiHttpService } from '@syndesis/ui/platform';
import { ConnectionConfigurationService } from '@syndesis/ui/connections/common/configuration/configuration.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';

interface AcquisitionResponseState {
  persist: string;
  spec: string;
}

interface AcquisitionResponse {
  redirectUrl: string;
  type: string;
  state: AcquisitionResponseState;
}

@Component({
  selector: 'syndesis-connection-detail-configuration',
  templateUrl: './configuration.component.html'
})
export class ConnectionDetailConfigurationComponent
  implements OnInit, OnChanges {
  @Input() connection: Connection;
  @Output() updated = new EventEmitter<Connection>();
  mode: 'view' | 'edit' = 'view';
  formModel: DynamicFormControlModel[];
  formGroup: FormGroup;
  message: String;
  messageOutcome: 'SUCCESS' | 'FAILURE' = 'SUCCESS';

  private connection$ = new Subject<Connection>();

  constructor(
    private configurationService: ConnectionConfigurationService,
    private formService: DynamicFormService,
    private apiHttpService: ApiHttpService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.route.fragment.subscribe(fragment => {
      if (fragment) {
        const outcome = JSON.parse(decodeURIComponent(fragment));
        this.message = outcome.message;
        this.messageOutcome = outcome.status;
        this.router.navigate([], { relativeTo: this.route });
        if (outcome.status === 'SUCCESS') {
          this.connection$.subscribe(conn => {
            this.connection$.unsubscribe();
            this.updated.emit(conn);
          });
        }
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.resetView(true);
    if (changes.connection) {
      this.connection$.next(changes.connection.currentValue);
    }
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
    this.formModel = this.configurationService.getFormModel(this.connection);
    this.formGroup = this.formService.createFormGroup(this.formModel);
    if (readOnly) {
      this.formGroup.disable();
    } else {
      this.formGroup.enable();
    }
  }

  reconnect() {
    const returnUrl = window.location.pathname;
    this.apiHttpService
      .setEndpointUrl(`/connectors/${this.connection.connectorId}/credentials`)
      .post<AcquisitionResponse>({ returnUrl })
      .subscribe(response => {
        document.cookie = response.state.spec;
        window.location.href = response.redirectUrl;
      });
  }
}
