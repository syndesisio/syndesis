import { Component, OnInit, ViewChild, TemplateRef, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { Store } from '@ngrx/store';

import { ModalService } from '@syndesis/ui/common';
import {
  ApiConnectorState,
  ApiConnectorStore,
  ApiConnectorActions,
  getApiConnectorState,
  CustomSwaggerConnectorRequest,
  CustomApiConnectorAuthSettings
} from '@syndesis/ui/customizations/api-connector';

enum WizardSteps {
  UploadSwagger = 1,
  ReviewApiConnector = 2,
  UpdateAuthSettings = 3,
  SubmitRequest = 4
}

@Component({
  selector: 'syndesis-api-connector-create',
  styleUrls: ['./api-connector-create.component.scss'],
  templateUrl: './api-connector-create.component.html'
})
export class ApiConnectorCreateComponent implements OnInit, OnDestroy {
  currentActiveStep = 1;
  apiConnectorState$: Observable<ApiConnectorState>;
  @ViewChild('cancelModalTemplate') cancelModalTemplate: TemplateRef<any>;

  private cancelModalId = 'create-cancellation-modal';

  constructor(
    private router: Router,
    private modalService: ModalService,
    private apiConnectorStore: Store<ApiConnectorStore>
  ) { }

  ngOnInit() {
    this.modalService.registerModal(this.cancelModalId, this.cancelModalTemplate);
    this.apiConnectorState$ = this.apiConnectorStore.select(getApiConnectorState);
    // Once the request validation results are yielded for the 1st time, we move user to step 2
    this.apiConnectorState$.map(apiConnectorState => apiConnectorState.createRequest)
      .first(request => !!request && !!request.actionsSummary)
      .subscribe(() => this.currentActiveStep = WizardSteps.ReviewApiConnector);
  }

  showCancelModal(): void {
    this.modalService.show(this.cancelModalId).then(modal => {
      if (modal.result) {
        this.router.navigate(['customizations', 'api-connector']);
      }
    });
  }

  onCancel(doCancel: boolean): void {
    this.modalService.hide(this.cancelModalId, doCancel);
  }

  onValidationRequest(request: CustomSwaggerConnectorRequest) {
    this.apiConnectorStore.dispatch(ApiConnectorActions.validateSwagger(request));
  }

  onReviewComplete(): void {
    this.currentActiveStep = WizardSteps.UpdateAuthSettings;
  }

  onAuthSetup(authSettings: CustomApiConnectorAuthSettings): void {
    this.apiConnectorStore.dispatch(ApiConnectorActions.updateAuthSettings(authSettings));
    this.currentActiveStep = WizardSteps.SubmitRequest;
  }

  onCreateComplete(event): void {
    //console.log(event);
  }

  ngOnDestroy() {
    this.modalService.unregisterModal(this.cancelModalId);
    this.apiConnectorStore.dispatch(ApiConnectorActions.createCancel());
  }
}
