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
  CustomSwaggerConnectorRequest
} from '@syndesis/ui/customizations/api-connector';

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
    private apiConnectorStore: Store<ApiConnectorStore>,
  ) { }

  ngOnInit() {
    this.modalService.registerModal(this.cancelModalId, this.cancelModalTemplate);
    this.apiConnectorState$ = this.apiConnectorStore.select(getApiConnectorState);
    // Once the request validation results are yielded for the 1st time, we move user to step 2
    this.apiConnectorState$.map(apiConnectorState => apiConnectorState.createRequest)
      .filter(request => !!request)
      .first(request => !!request.validationDetails)
      .subscribe(() => this.currentActiveStep = 2);
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
    this.currentActiveStep = 3;
  }

  onAuthSetup(): void {
    this.currentActiveStep = 4;
  }

  onCreateComplete(event): void {
    //console.log(event);
  }

  ngOnDestroy() {
    this.modalService.unregisterModal(this.cancelModalId);
  }
}
