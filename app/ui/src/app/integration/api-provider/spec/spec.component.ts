import { Component, OnInit, OnDestroy, ViewChild, TemplateRef } from '@angular/core';
// import { Observable } from 'rxjs';
// import { first, map } from 'rxjs/operators';
import { I18NService } from '@syndesis/ui/platform';
import { ModalService, NavigationService, OpenApiData } from '@syndesis/ui/common';
// import {
//   ApiConnectorActions,
//   ApiConnectorState,
//   ApiConnectorStore, CustomApiConnectorAuthSettings,
//   CustomConnectorRequest,
//   getApiConnectorState
// } from '@syndesis/ui/customizations/api-connector/index';
import { ApiDefinition, /*ApiEditorComponent*/ } from 'apicurio-design-studio';
// import { Store } from '@ngrx/store';
import { WindowRef } from '@syndesis/ui/customizations/window-ref';
import { Router } from '@angular/router';
import * as YAML from 'yamljs';

enum WizardSteps {
  UploadSwagger = 1,
  ReviewApiConnector = 2,
  UpdateAuthSettings = 3,
  SubmitRequest = 4
}

@Component({
  selector: 'syndesis-integration-api-provider-spec',
  templateUrl: './spec.component.html',
  styleUrls: ['../../integration-common.scss', './spec.component.scss']
})
export class ApiProviderSpecComponent implements OnInit, OnDestroy {
  currentActiveStep = WizardSteps.UploadSwagger;
  // apiConnectorState$: Observable<ApiConnectorState>;
  displayDefinitionEditor = false;
  editorHasChanges = false;
  // validationResponse: CustomConnectorRequest;
  useApiFileImport = true; // default to file import (false is a URL import)
  apiUrl: string;
  apiFile: File;

  // @ViewChild('_apiEditor') _apiEditor: ApiEditorComponent;
  apiDef: ApiDefinition;

  @ViewChild('cancelEditorModalTemplate') cancelEditorModalTemplate: TemplateRef<any>;
  @ViewChild('cancelModalTemplate') cancelModalTemplate: TemplateRef<any>;

  private cancelEditorModalId = 'create-cancel-editor-modal';
  private cancelModalId = 'create-cancellation-modal';

  constructor(
    // private apiConnectorStore: Store<ApiConnectorStore>,
    private i18NService: I18NService,
    private modalService: ModalService,
    private nav: NavigationService,
    private router: Router,
    private winRef: WindowRef
  ) {
    this.winRef.nativeWindow.dump = YAML.dump;
    this.apiDef = new ApiDefinition();
    this.apiDef.createdBy = 'user1';
    this.apiDef.createdOn = new Date();
    this.apiDef.tags = [];
    this.apiDef.description = '';
    this.apiDef.id = 'api-1';
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
    // this.apiConnectorState$ = this.apiConnectorStore.select(
    //   getApiConnectorState
    // );

    /*// Once the request validation results are yielded for the 1st time, we move user to step 2
    this.apiConnectorState$.map(apiConnectorState => apiConnectorState.createRequest)
      .subscribe( apiConnectorState => {
        if ( apiConnectorState.actionsSummary ) {
          this.validationResponse = apiConnectorState;

          // move to review step from first step
          if ( this.currentActiveStep == 1 ) {
            this.currentActiveStep = WizardSteps.ReviewApiConnector;
          }

          if ( this.currentActiveStep == WizardSteps.ReviewApiConnector ) {
            // read in API spec and perform validation if current step is the review step
            if ( apiConnectorState.specificationFile ) {
              const reader = new FileReader();

              reader.onload = () => {
                this.apiDef.spec = reader.result;
                this.apiDef.name = apiConnectorState.name;
              };

              reader.readAsText( apiConnectorState.specificationFile );
            } else {
              this.apiDef.spec = apiConnectorState.configuredProperties.specification;
            }
          }
        }
      });

    // Once the request object is flagged as 'isComplete', we redirect the user to the main listing
    this.apiConnectorState$
      .pipe(map(apiConnectorState => apiConnectorState.createRequest))
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
    } as CustomConnectorRequest;

    this.apiConnectorStore.dispatch(
      ApiConnectorActions.validateSwagger(request)
    );*/
  }

  /**
   * Called when the upload step changes import method (file or URL).
   *
   * @param fileImport `true` if import should be done by file; otherwise it will be a URL import.
   */
  onApiFileImportChanged( fileImport: boolean ) {
    this.useApiFileImport = fileImport;
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

  onSpecification(specification: OpenApiData) {
    /*this.apiConnectorStore.dispatch(
      ApiConnectorActions.validateSwagger(request)
    );*/
    console.log(specification);
  }

  onReviewComplete({event: event, displayEditor: displayEditor}): void {
    // Check if request is to show editor or not
    if (displayEditor === true) {
      this.displayDefinitionEditor = true;

    } else {
      this.displayDefinitionEditor = false;
      this.currentActiveStep = WizardSteps.UpdateAuthSettings;
    }
  }

  /**
   * Called when the back button has been pressed on one of the steps.
   */
  onBackPressed() {
    this.currentActiveStep -= 1;

    /*// clear out review results when going back to upload step
    if ( this.currentActiveStep === WizardSteps.UploadSwagger ) {
      this.validationResponse.actionsSummary = null;
      this.validationResponse.errors = [];
      this.validationResponse.warnings = [];

      // make sure for URL import that the file has been cleared out
      if ( !this.useApiFileImport ) {
        this.validationResponse.specificationFile = null;
      }
    }*/
  }

  /**
   * Called when the upload step file selection changes.
   *
   * @param newFile the selected file
   */
  onApiFileChanged( newFile: File ): void {
    this.apiFile = newFile;
  }

  /**
   * Called when the upload step has a change to the URL.
   *
   * @param newUrl the new URL
   */
  onApiUrlChanged( newUrl: string ): void {
    this.apiUrl = newUrl;
  }

  /*onCreateComplete(customConnectorRequest: CustomConnectorRequest): void {
    // update request if changes were made in editor
    if ( this.editorHasChanges ) {
      customConnectorRequest.configuredProperties.specification = JSON.stringify( this.apiDef.spec );
    }

    this.apiConnectorStore.dispatch(
      ApiConnectorActions.create(customConnectorRequest)
    );
  }*/

  ngOnDestroy() {
    this.modalService.unregisterModal(this.cancelEditorModalId);
    this.modalService.unregisterModal(this.cancelModalId);
    // this.apiConnectorStore.dispatch(ApiConnectorActions.createCancel());
    this.nav.show();
  }

  private redirectBack(): void {
    this.router.navigate(['customizations', 'api-connector']);
  }
}
