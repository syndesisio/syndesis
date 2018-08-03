import { map, first } from 'rxjs/operators';
import {
  Component,
  OnInit,
  ViewChild,
  TemplateRef,
  OnDestroy
} from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';

import { WindowRef } from '@syndesis/ui/customizations/window-ref';
import * as YAML from 'yamljs';

import { ModalService, NavigationService } from '@syndesis/ui/common';
import {
  ApiConnectorState,
  ApiConnectorStore,
  ApiConnectorActions,
  getApiConnectorState,
  CustomConnectorRequest,
  CustomApiConnectorAuthSettings
} from '@syndesis/ui/customizations/api-connector';
import { I18NService } from '@syndesis/ui/platform';

import { ApiEditorComponent, ApiDefinition } from 'apicurio-design-studio';
import { OtCommand } from 'oai-ts-commands';

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
  displayDefinitionEditor = false;
  editorHasChanges = false;
  validationResponse: CustomConnectorRequest;
  enableEditor = true;

  @ViewChild('_apiEditor') _apiEditor: ApiEditorComponent;
  apiDef: ApiDefinition;

  @ViewChild('cancelEditorModalTemplate') cancelEditorModalTemplate: TemplateRef<any>;
  @ViewChild('cancelModalTemplate') cancelModalTemplate: TemplateRef<any>;

  private cancelEditorModalId = 'create-cancel-editor-modal';
  private cancelModalId = 'create-cancellation-modal';

  constructor(
    private apiConnectorStore: Store<ApiConnectorStore>,
    private i18NService: I18NService,
    private modalService: ModalService,
    private nav: NavigationService,
    private router: Router,
    private winRef: WindowRef
  ) {
    this.winRef.nativeWindow.dump = YAML.dump;
  }

  public onUserChange(command: OtCommand): void {
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
    this.apiConnectorState$ = this.apiConnectorStore.select(
      getApiConnectorState
    );

    // Once the request validation results are yielded for the 1st time, we move user to step 2
    this.apiConnectorState$.map(apiConnectorState => apiConnectorState.createRequest)
      .first(request => !!request && !!request.actionsSummary)
      .subscribe( apiConnectorState => {
        this.validationResponse = apiConnectorState;

        this.apiDef = new ApiDefinition();
        this.apiDef.createdBy = 'user1';
        this.apiDef.createdOn = new Date();
        this.apiDef.tags = [];
        this.apiDef.description = '';
        this.apiDef.id = 'api-1';
        this.currentActiveStep = WizardSteps.ReviewApiConnector;

        if ( apiConnectorState.specificationFile ) {
          const reader = new FileReader();

          reader.onload = () => {
            this.apiDef.spec = reader.result;
            this.apiDef.name = apiConnectorState.name;
          };

          reader.readAsText( apiConnectorState.specificationFile );
        } else {
          this.enableEditor = false;

          const fetch$ = Observable
            .from(fetch(apiConnectorState.configuredProperties.specification))
            .flatMap(response => response.json());

          fetch$.subscribe(spec => {
            this.apiDef.spec = spec;
            this.enableEditor = true;
          });
        }
      });

    // Once the request object is flagged as 'isComplete', we redirect the user to the main listing
    this.apiConnectorState$
      .pipe(map(apiConnectorState => apiConnectorState.createRequest))
      .pipe(first(request => !!request && request.isComplete))
      .subscribe(() => this.redirectBack());
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
    // save current state of editor
    const value = this._apiEditor.getValue();
    this.apiDef.spec = value[ 'spec' ]; // used in onCreateComplete

    // go back to review step
    this.displayDefinitionEditor = false;

    const blob = new Blob([JSON.stringify(this.apiDef.spec)], {type : 'application/json'}) as any;
    const file = new File([blob], this.validationResponse.specificationFile.name, {type : 'application/json'});

    const request = {
      ...this.validationResponse,
      specificationFile: file
    } as CustomConnectorRequest;

    this.apiConnectorStore.dispatch(
      ApiConnectorActions.validateSwagger(request)
    );
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

  onValidationRequest(request: CustomConnectorRequest) {
    this.apiConnectorStore.dispatch(
      ApiConnectorActions.validateSwagger(request)
    );
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

  onAuthSetup(authSettings: CustomApiConnectorAuthSettings): void {
    this.apiConnectorStore.dispatch(
      ApiConnectorActions.updateAuthSettings(authSettings)
    );
    this.currentActiveStep = WizardSteps.SubmitRequest;
  }

  onCreateComplete(customConnectorRequest: CustomConnectorRequest): void {
    // update request if changes were made in editor
    if ( this.editorHasChanges ) {
      customConnectorRequest.configuredProperties.specification = JSON.stringify( this.apiDef.spec );
    }

    this.apiConnectorStore.dispatch(
      ApiConnectorActions.create(customConnectorRequest)
    );
  }

  ngOnDestroy() {
    this.modalService.unregisterModal(this.cancelEditorModalId);
    this.modalService.unregisterModal(this.cancelModalId);
    this.apiConnectorStore.dispatch(ApiConnectorActions.createCancel());
    this.nav.show();
  }

  private redirectBack(): void {
    this.router.navigate(['customizations', 'api-connector']);
  }
}
