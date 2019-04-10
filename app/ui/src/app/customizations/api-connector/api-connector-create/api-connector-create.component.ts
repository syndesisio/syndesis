import {
  Component,
  OnInit,
  ViewChild,
  TemplateRef,
  OnDestroy
} from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { Store, select } from '@ngrx/store';
import {
  ModalService,
  NavigationService,
  OpenApiUploadSpecification,
  OpenApiValidationErrorMessage
} from '@syndesis/ui/common';
import {
  ApiConnectorStore,
  ApiConnectorActions,
  ApiConnectorWizardStep,
  getApiConnectorWizardStep,
  getApiConnectorSpecificationForEditor,
  getApiConnectorUploadSpecification,
  getApiConnectorLoading,
  getApiConnectorValidationError,
  getApiConnectorRequest, CustomApiConnectorAuthSettings, getShowApiEditor, getApiConnectorState,
} from '@syndesis/ui/customizations/api-connector';
import {
  ApiConnectorState,
  CustomConnectorRequest
} from '@syndesis/ui/customizations/api-connector/api-connector.models';

@Component({
  selector: 'syndesis-api-connector-create',
  styleUrls: ['./api-connector-create.component.scss'],
  templateUrl: './api-connector-create.component.html'
})
export class ApiConnectorCreateComponent implements OnInit, OnDestroy {
  ApiConnectorWizardStep = ApiConnectorWizardStep;

  loading$: Observable< boolean >;
  currentActiveStep$: Observable<ApiConnectorWizardStep>;
  uploadSpecification$: Observable<OpenApiUploadSpecification>;
  validationErrors$: Observable<OpenApiValidationErrorMessage[]>;
  specificationForEditor$: Observable<string>;
  createRequest$: Observable<CustomConnectorRequest>;
  showApiEditor$: Observable<boolean>;
  apiConnectorState$: Observable<ApiConnectorState>;

  @ViewChild('cancelModalTemplate') cancelModalTemplate: TemplateRef<any>;
  private cancelModalId = 'create-cancellation-modal';

  constructor(
    private apiConnectorStore: Store<ApiConnectorStore>,
    private modalService: ModalService,
    private nav: NavigationService,
    private router: Router,
  ) {
    // nothing to do
  }

  ngOnInit() {
    this.modalService.registerModal(
      this.cancelModalId,
      this.cancelModalTemplate
    );

    this.loading$ = this.apiConnectorStore.pipe(select(getApiConnectorLoading));
    this.currentActiveStep$ = this.apiConnectorStore.pipe(select(getApiConnectorWizardStep));
    this.uploadSpecification$ = this.apiConnectorStore.pipe(select(getApiConnectorUploadSpecification));
    this.validationErrors$ = this.apiConnectorStore.pipe(select(getApiConnectorValidationError));
    this.specificationForEditor$ = this.apiConnectorStore.pipe(select(getApiConnectorSpecificationForEditor));
    this.createRequest$ = this.apiConnectorStore.pipe(select(getApiConnectorRequest));
    this.showApiEditor$ = this.apiConnectorStore.pipe(select(getShowApiEditor));
    this.apiConnectorState$ = this.apiConnectorStore.pipe(select(getApiConnectorState));

    this.nav.hide();
  }

  ngOnDestroy() {
    this.modalService.unregisterModal(this.cancelModalId);
    this.apiConnectorStore.dispatch(ApiConnectorActions.createCancel());
    this.nav.show();
  }

  onAuthSetup(authSettings: CustomApiConnectorAuthSettings): void {
    this.apiConnectorStore.dispatch(
      ApiConnectorActions.updateAuthSettings(authSettings)
    );
  }

  /**
   * Called when the back button has been pressed on one of the steps.
   */
  onBackPressed() {
    this.apiConnectorStore.dispatch(
      ApiConnectorActions.previousStep()
    );
  }

  onCancel(doCancel: boolean): void {
    this.modalService.hide(this.cancelModalId, doCancel);
  }

  onCloseApiEditor() {
    this.apiConnectorStore.dispatch(
      ApiConnectorActions.cancelEditSpecification()
    );
  }

  onCreateComplete(customConnectorRequest: CustomConnectorRequest): void {
    this.apiConnectorStore.dispatch(
      ApiConnectorActions.create(customConnectorRequest)
    );
  }

  onEditSpecification() {
    this.apiConnectorStore.dispatch(
      ApiConnectorActions.editSpecification()
    );
  }

  onSpecificationEdited(spec: string) {
    this.apiConnectorStore.dispatch(
      ApiConnectorActions.updateSpecification(spec)
    );
  }

  onStepDone() {
    this.apiConnectorStore.dispatch(
      ApiConnectorActions.nextStep()
    );
  }

  onUploadSpecificationChange(uploadSpecification: OpenApiUploadSpecification) {
    this.apiConnectorStore.dispatch(
      ApiConnectorActions.uploadSpecification(uploadSpecification)
    );
  }

  showCancelModal(): void {
    this.modalService.show(this.cancelModalId).then(modal => {
      if (modal.result) {
        this.redirectBack();
      }
    });
  }

  private redirectBack(): void {
    this.router.navigate(['customizations', 'api-connector']);
  }
}
