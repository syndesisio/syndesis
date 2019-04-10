import {
  Component,
  OnDestroy,
  OnInit,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import {
  ModalService,
  NavigationService,
  OpenApiUploaderValueType,
  OpenApiUploadSpecification,
  OpenApiValidationErrorMessage,
} from '@syndesis/ui/common';
import { Store, select } from '@ngrx/store';
import { Router } from '@angular/router';
import { ApiProviderActions } from '@syndesis/ui/integration/api-provider/api-provider.actions';
import {
  ApiProviderStore,
  getApiProviderCreationError,
  getApiProviderIntegrationName,
  getApiProviderSpecificationForEditor,
  getApiProviderSpecificationTitle,
  getApiProviderUploadSpecification,
  getApiProviderValidationError,
  getApiProviderLoading,
  getApiProviderValidationResponse,
  getApiProviderWizardStep,
  getApiProviderIntegrationDescription,
} from '@syndesis/ui/integration/api-provider/api-provider.reducer';
import {
  ApiProviderValidationResponse,
  ApiProviderWizardSteps,
} from '@syndesis/ui/integration/api-provider/api-provider.models';
import { Observable, Subscription } from 'rxjs';
import { ActionReducerError } from '@syndesis/ui/platform';
import {
  CurrentFlowService,
  INTEGRATION_CANCEL_CLICKED,
  INTEGRATION_REMOVE_STEP,
} from '../../edit-page';

@Component({
  selector: 'syndesis-integration-api-provider-spec',
  templateUrl: './integration-api-provider-creation-page.component.html',
  styleUrls: [
    '../../integration-common.scss',
    './integration-api-provider-creation-page.component.scss',
  ],
})
export class ApiProviderSpecComponent implements OnInit, OnDestroy {
  ApiProviderWizardSteps = ApiProviderWizardSteps;
  OpenApiUploaderValueType = OpenApiUploaderValueType;
  validationResponse: ApiProviderValidationResponse;

  loading$: Observable<boolean>;
  currentActiveStep$: Observable<ApiProviderWizardSteps>;
  uploadSpecification$: Observable<OpenApiUploadSpecification>;
  validationErrors$: Observable<OpenApiValidationErrorMessage[]>;
  validationResponse$: Observable<ApiProviderValidationResponse>;
  integrationName$: Observable<string>;
  specificationForEditor$: Observable<string>;
  specificationTitle$: Observable<string>;
  integrationDescription$: Observable<string>;
  creationError$: Observable<ActionReducerError>;
  flowSubscription: Subscription;
  doingRedirect = false;

  @ViewChild('cancelModalTemplate') cancelModalTemplate: TemplateRef<any>;
  private cancelModalId = 'api-provider-create-cancellation-modal';

  constructor(
    private apiProviderStore: Store<ApiProviderStore>,
    private modalService: ModalService,
    private currentFlowService: CurrentFlowService,
    private nav: NavigationService,
    private router: Router
  ) {}

  ngOnInit() {
    this.modalService.registerModal(
      this.cancelModalId,
      this.cancelModalTemplate
    );

    this.flowSubscription = this.currentFlowService.events.subscribe(event => {
      if (event.kind === INTEGRATION_CANCEL_CLICKED) {
        this.showCancelModal();
      }
    });
    this.loading$ = this.apiProviderStore.pipe(select(getApiProviderLoading));
    this.currentActiveStep$ = this.apiProviderStore.pipe(
      select(getApiProviderWizardStep)
    );
    this.uploadSpecification$ = this.apiProviderStore.pipe(
      select(getApiProviderUploadSpecification)
    );
    this.validationErrors$ = this.apiProviderStore.pipe(
      select(getApiProviderValidationError)
    );
    this.validationResponse$ = this.apiProviderStore.pipe(
      select(getApiProviderValidationResponse)
    );
    this.integrationName$ = this.apiProviderStore.pipe(
      select(getApiProviderIntegrationName)
    );
    this.specificationForEditor$ = this.apiProviderStore.pipe(
      select(getApiProviderSpecificationForEditor)
    );
    this.specificationTitle$ = this.apiProviderStore.pipe(
      select(getApiProviderSpecificationTitle)
    );
    this.integrationDescription$ = this.apiProviderStore.pipe(
      select(getApiProviderIntegrationDescription)
    );
    this.creationError$ = this.apiProviderStore.pipe(
      select(getApiProviderCreationError)
    );

    this.nav.hide();
  }

  showCancelModal(): void {
    this.modalService.show(this.cancelModalId).then(modal => {
      if (modal.result) {
        this.redirectBack();
      }
    });
  }

  onCancelModalCancel(doCancel: boolean): void {
    this.modalService.hide(this.cancelModalId, doCancel);
  }

  onUploadSpecificationChange(uploadSpecification: OpenApiUploadSpecification) {
    this.apiProviderStore.dispatch(
      ApiProviderActions.uploadSpecification(uploadSpecification)
    );
  }

  onSpecificationEdited(spec: string) {
    this.apiProviderStore.dispatch(
      ApiProviderActions.updateSpecification(spec)
    );
  }

  onStepDone() {
    this.apiProviderStore.dispatch(ApiProviderActions.nextStep());
  }

  onEditSpecification() {
    this.apiProviderStore.dispatch(ApiProviderActions.editSpecification());
  }

  updateIntegrationName(name: string) {
    this.apiProviderStore.dispatch(
      ApiProviderActions.updateIntegrationName(name)
    );
  }

  updateIntegrationDescription(description: string) {
    this.apiProviderStore.dispatch(
      ApiProviderActions.updateIntegrationDescription(description)
    );
  }

  createIntegration() {
    this.apiProviderStore.dispatch(ApiProviderActions.createIntegration());
  }

  /**
   * Called when the back button has been pressed on one of the steps.
   */
  onBackPressed() {
    this.apiProviderStore.dispatch(ApiProviderActions.previousStep());
  }

  ngOnDestroy() {
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
    this.modalService.unregisterModal(this.cancelModalId);
    this.apiProviderStore.dispatch(ApiProviderActions.createCancel());
    // If the user canceled out of the wizard we're still in the editor
    if (!this.doingRedirect) {
      this.nav.show();
    }
  }

  private redirectBack(): void {
    this.doingRedirect = true;
    // Remove the start connection that enables the API provider wizard
    // and go back to the regular editor
    this.currentFlowService.events.emit({
      kind: INTEGRATION_REMOVE_STEP,
      position: 0,
      onSave: () => {
        this.router.navigate(['/integrations/create/save-or-add-step']);
      },
    });
  }
}
