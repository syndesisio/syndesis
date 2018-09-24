import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import {
  ModalService,
  NavigationService,
  OpenApiValidationResponse,
  OpenApiUploaderValueType,
  OpenApiUploadSpecification,
  OpenApiValidationError
} from '@syndesis/ui/common';
import { Store } from '@ngrx/store';
import { WindowRef } from '@syndesis/ui/customizations/window-ref';
import { Router } from '@angular/router';
import * as YAML from 'yamljs';
import { ApiProviderActions } from '@syndesis/ui/integration/api-provider/api-provider.actions';
import {
  ApiProviderStore, getApiProviderUploadSpecification,
  getApiProviderValidationError,
  getApiProviderValidationLoading,
  getApiProviderValidationResponse, getApiProviderWizardStep
} from '@syndesis/ui/integration/api-provider/api-provider.reducer';
import { ApiProviderWizardSteps } from '@syndesis/ui/integration/api-provider/api-provider.models';
import { Observable } from 'rxjs';

@Component({
  selector: 'syndesis-integration-api-provider-spec',
  templateUrl: './spec.component.html',
  styleUrls: ['../../integration-common.scss', './spec.component.scss']
})
export class ApiProviderSpecComponent implements OnInit, OnDestroy {
  ApiProviderWizardSteps = ApiProviderWizardSteps;
  OpenApiUploaderValueType = OpenApiUploaderValueType;
  displayDefinitionEditor = false;
  editorHasChanges = false;
  validationResponse: OpenApiValidationResponse;

  currentActiveStep$: Observable<ApiProviderWizardSteps>;
  uploadSpecification$: Observable<OpenApiUploadSpecification>;
  validationError$: Observable<OpenApiValidationError>;
  validationResponse$: Observable<OpenApiValidationResponse>;
  validationLoading$: Observable<boolean>;

  // @ViewChild('_apiEditor') _apiEditor: ApiEditorComponent;

  @ViewChild('cancelEditorModalTemplate') cancelEditorModalTemplate: TemplateRef<any>;
  @ViewChild('cancelModalTemplate') cancelModalTemplate: TemplateRef<any>;

  private cancelEditorModalId = 'create-cancel-editor-modal';
  private cancelModalId = 'create-cancellation-modal';

  constructor(
    private apiProviderStore: Store<ApiProviderStore>,
    private modalService: ModalService,
    private nav: NavigationService,
    private router: Router,
    private winRef: WindowRef
  ) {
    this.winRef.nativeWindow.dump = YAML.dump;
  }

  public onUserChange(): void {
    this.editorHasChanges = true;
  }

  public showDefinitionEditor(): boolean {
    return true;
  }

  ngOnInit() {
    this.modalService.registerModal(
      this.cancelModalId,
      this.cancelModalTemplate
    );
    this.modalService.registerModal(
      this.cancelEditorModalId,
      this.cancelEditorModalTemplate
    );

    this.currentActiveStep$ = this.apiProviderStore.select(getApiProviderWizardStep);
    this.uploadSpecification$ = this.apiProviderStore.select(getApiProviderUploadSpecification);
    this.validationError$ = this.apiProviderStore.select(getApiProviderValidationError);
    this.validationResponse$ = this.apiProviderStore.select(getApiProviderValidationResponse);
    this.validationLoading$ = this.apiProviderStore.select(getApiProviderValidationLoading);

    this.currentActiveStep$.subscribe(step => {
      switch (step) {
        case ApiProviderWizardSteps.ReviewApiProvider: {
          this.apiProviderStore.dispatch(
            ApiProviderActions.validateSwagger()
          );
          break;
        }
        default:
          break;
      }
    })
    this.nav.hide();
  }

  showCancelModal(): void {
    this.modalService.show(this.cancelModalId).then(modal => {
      if (modal.result) {
        this.redirectBack();
      }
    });
  }

  onCancel(doCancel: boolean): void {
    this.modalService.hide(this.cancelModalId, doCancel);
  }

  onUploadSpecificationChange(spec: OpenApiUploadSpecification) {
    this.apiProviderStore.dispatch(
      ApiProviderActions.updateSpecification(spec)
    );
  }

  onStepDone() {
    this.apiProviderStore.dispatch(
      ApiProviderActions.nextStep()
    );
  }

  /**
   * Called when the back button has been pressed on one of the steps.
   */
  onBackPressed() {
    this.apiProviderStore.dispatch(
      ApiProviderActions.previousStep()
    );
  }

  /*onCreateComplete(customProviderRequest: CustomProviderRequest): void {
    // update request if changes were made in editor
    if ( this.editorHasChanges ) {
      customProviderRequest.configuredProperties.specification = JSON.stringify( this.apiDef.spec );
    }

    this.apiProviderStore.dispatch(
      ApiProviderActions.create(customProviderRequest)
    );
  }*/

  ngOnDestroy() {
    this.modalService.unregisterModal(this.cancelEditorModalId);
    this.modalService.unregisterModal(this.cancelModalId);
    this.apiProviderStore.dispatch(ApiProviderActions.createCancel());
    this.nav.show();
  }

  private redirectBack(): void {
    this.router.navigate(['/integrations']);
  }
}
