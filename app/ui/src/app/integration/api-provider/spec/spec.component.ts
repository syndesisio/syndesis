import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { I18NService } from '@syndesis/ui/platform';
import { ModalService, NavigationService, OpenApiUploaderValueType, OpenApiUploadSpecification } from '@syndesis/ui/common';
import { Store } from '@ngrx/store';
import { WindowRef } from '@syndesis/ui/customizations/window-ref';
import { Router } from '@angular/router';
import * as YAML from 'yamljs';
import { ApiProviderActions } from '@syndesis/ui/integration/api-provider/api-provider.actions';
import { ApiProviderStore, getApiProviderState } from '@syndesis/ui/integration/api-provider/api-provider.reducer';
import { ApiProviderData, ApiProviderState, ApiProviderValidationError } from '@syndesis/ui/integration/api-provider/api-provider.models';
import { Observable } from 'rxjs';
import { ApiDefinition } from 'apicurio-design-studio';

enum WizardSteps {
  UploadSpecification = 1,
  ReviewApiProvider = 2,
  SubmitRequest = 3
}

@Component({
  selector: 'syndesis-integration-api-provider-spec',
  templateUrl: './spec.component.html',
  styleUrls: ['../../integration-common.scss', './spec.component.scss']
})
export class ApiProviderSpecComponent implements OnInit, OnDestroy {
  OpenApiUploaderValueType = OpenApiUploaderValueType;
  currentActiveStep = WizardSteps.UploadSpecification;
  apiProviderState$: Observable<ApiProviderState>;
  displayDefinitionEditor = false;
  editorHasChanges = false;
  validationResponse: ApiProviderData;
  uploaderValue: OpenApiUploadSpecification;
  apiDef: ApiDefinition;
  validationErrors: ApiProviderValidationError[];

  // @ViewChild('_apiEditor') _apiEditor: ApiEditorComponent;

  @ViewChild('cancelEditorModalTemplate') cancelEditorModalTemplate: TemplateRef<any>;
  @ViewChild('cancelModalTemplate') cancelModalTemplate: TemplateRef<any>;

  private cancelEditorModalId = 'create-cancel-editor-modal';
  private cancelModalId = 'create-cancellation-modal';

  constructor(
    private apiProviderStore: Store<ApiProviderStore>,
    private i18NService: I18NService,
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

    this.apiProviderState$ = this.apiProviderStore.select(
      getApiProviderState
    );

    this.apiProviderState$.map(apiProviderState => apiProviderState.errors)
      .subscribe(errors => this.validationErrors = errors);

    // Once the request validation results are yielded for the 1st time, we move user to step 2
    this.apiProviderState$.map(apiProviderState => apiProviderState.createRequest)
      .subscribe( apiProviderState => {
        if ( apiProviderState.actionsSummary ) {
          this.validationResponse = apiProviderState;

          // move to review step from first step
          if ( this.currentActiveStep == WizardSteps.UploadSpecification ) {
            this.currentActiveStep = WizardSteps.ReviewApiProvider;
          }

          if ( this.currentActiveStep == WizardSteps.ReviewApiProvider ) {
            this.apiDef.spec = apiProviderState.configuredProperties.specification;
          }
        }
      });

    /*
    // Once the request object is flagged as 'isComplete', we redirect the user to the main listing
    this.apiProviderState$
      .pipe(map(apiProviderState => apiProviderState.createRequest))
      .pipe(first(request => !!request && request.isComplete))
      .subscribe(() => this.redirectBack());*/
    this.nav.hide();
  }

  /**
   * @returns {boolean} `true` if the API Curio editor component is showing or should be shown
   */
  get showApiEditor(): boolean {
    return this.currentActiveStep === 2 && this.displayDefinitionEditor === true;
  }

  /**
   * Shows the quit editor confirmation modal only if there are changes in the API Curio editor.
   */
  showCancelApiEditorModal(): void {
    // show cancel dialog only if editor has changes
    if ( this.editorHasChanges ) {
      this.modalService.show( this.cancelEditorModalId ).then( modal => {
        if ( modal.result ) {
          // go back to review step without using updated API from editor
          this.editorHasChanges = false;
          this.displayDefinitionEditor = false;
        } } );
    } else {
      this.displayDefinitionEditor = false;
    }
  }

  /**
   * @param {boolean} doCancel `true` if the modal should be closed
   */
  onCancelApiEditor( doCancel: boolean ): void {
    this.modalService.hide( this.cancelEditorModalId, doCancel );
  }

  /**
   * Called when the API Curio editor componenent should be closed and the updated API spec should be used.
   */
  onDoneEditing(): void {
    /*// save current state of editor
    const value = this._apiEditor.getValue();
    this.apiDef.spec = value[ 'spec' ]; // used in onCreateComplete
    this.editorHasChanges = false;

    // go back to review step
    this.displayDefinitionEditor = false;

    const fileName = () => {
      // use name of file being uploaded
      if ( this.validationResponse.specificationFile.name && this.validationResponse.specificationFile.name ) {
        return this.validationResponse.specificationFile.name;
      }

      // since URL spec was edited we need to create a file from the spec
      if ( this.apiDef.spec && this.apiDef.spec.info && this.apiDef.spec.info.title ) {
        // remove whitespace from spec title
        return this.apiDef.spec.info.title.replace( /\s+/g, '' ) + '-edited-from-url';
      }

      // should not happen
      return 'api-edited-from-url';
    };

    const blob = new Blob([JSON.stringify(this.apiDef.spec)], {type : 'application/json'}) as any;
    const file = new File([blob], fileName(), {type : 'application/json'});

    // clear out current validation and request a new one be done
    const request = {
      ...this.validationResponse,
      specificationFile: file,
      actionsSummary: {},
      errors: [],
      warnings: []
    } as CustomProviderRequest;

    this.apiProviderStore.dispatch(
      ApiProviderActions.validateSwagger(request)
    );*/
  }

  get apiName(): string {
    if ( this.apiDef && this.apiDef.name ) {
      return this.apiDef.name;
    }

    return '';
  }

  /**
   * @returns {string} the title to use when displaying the API Curio editor component
   */
  get editorTitle(): string {
    if ( this.apiName ) {
      return this.i18NService.localize( 'customizations.api-client-connectors.edit-specific-api-definition',
        [ this.apiDef.name ] );
    }

    return this.i18NService.localize( 'customizations.api-client-connectors.edit-api-definition' );
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

  onSpecification() {
    if (this.uploaderValue.type === OpenApiUploaderValueType.Spec) {
      this.currentActiveStep = WizardSteps.ReviewApiProvider;
      this.apiDef = this.uploaderValue.spec as ApiDefinition;
    } else {
      this.apiProviderStore.dispatch(
        ApiProviderActions.validateSwagger(this.uploaderValue)
      );
    }
  }

  onReviewComplete({event: event, displayEditor: displayEditor}): void {
    // Check if request is to show editor or not
    if (displayEditor === true) {
      this.displayDefinitionEditor = true;

    } else {
      this.displayDefinitionEditor = false;
      this.currentActiveStep = WizardSteps.SubmitRequest;
    }
  }

  /**
   * Called when the back button has been pressed on one of the steps.
   */
  onBackPressed() {
    this.currentActiveStep -= 1;

    /*// clear out review results when going back to upload step
    if ( this.currentActiveStep === WizardSteps.UploadSpecification ) {
      this.validationResponse.actionsSummary = null;
      this.validationResponse.errors = [];
      this.validationResponse.warnings = [];

      // make sure for URL import that the file has been cleared out
      if ( !this.useApiFileImport ) {
        this.validationResponse.specificationFile = null;
      }
    }*/
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
