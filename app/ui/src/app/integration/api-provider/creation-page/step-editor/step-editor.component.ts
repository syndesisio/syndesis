import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { ApiDefinition } from 'apicurio-design-studio';
import { ModalService } from '@syndesis/ui/common';

@Component({
  selector: 'api-provider-creation-step-editor',
  templateUrl: './step-editor.component.html',
  styles: [
    `
      .apiprovider-editor {
        height: calc(100% - 40px);
        margin: 20px;
      }
    `
  ]
})
export class StepEditorComponent implements OnInit, OnDestroy {
  @Input('specification')
  set specification(specification: string) {
    this._specification = specification;
    this.specificationTitle = JSON.parse(this._specification).info.title;
  }
  get specification() {
    return this._specification;
  }
  @Output() onBack = new EventEmitter<boolean>();
  @Output() onCancel = new EventEmitter<boolean>();
  @Output() onSave = new EventEmitter<string>();

  _specification: string;
  specificationTitle: string;

  @ViewChild('cancelModalTemplate') cancelModalTemplate: TemplateRef<any>;
  private cancelModalId = 'api-provider-create-editor-cancellation-modal';

  constructor(
    private modalService: ModalService,
  ) {}

  ngOnInit() {
    this.modalService.registerModal(
      this.cancelModalId,
      this.cancelModalTemplate
    );
  }

  showCancelModal(): void {
    this.modalService.show(this.cancelModalId).then(modal => {
      if (modal.result) {
        this.onCancel.emit();
      }
    });
  }

  onCancelModalCancel(doCancel: boolean): void {
    this.modalService.hide(this.cancelModalId, doCancel);
  }

  onApiDefinitionChange(apiDefinition: ApiDefinition) {
    this.onSave.emit(JSON.stringify(apiDefinition.spec));
  }

  ngOnDestroy() {
    this.modalService.unregisterModal(this.cancelModalId);
  }
}
