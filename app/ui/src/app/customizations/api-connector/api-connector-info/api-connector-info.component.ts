import { Component, OnInit, Input, Output, EventEmitter, ElementRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import {
  CustomConnectorRequest,
  ApiConnectorData,
  ApiConnectorState
} from '@syndesis/ui/customizations/api-connector';

@Component({
  selector: 'syndesis-api-connector-info',
  templateUrl: './api-connector-info.component.html',
  styleUrls: ['./api-connector-info.component.scss']
})
export class ApiConnectorInfoComponent implements OnInit {
  @Input() apiConnectorState: ApiConnectorState;
  @Input() apiConnectorData: ApiConnectorData;
  @Output() update = new EventEmitter<CustomConnectorRequest>();
  @Output() onBack = new EventEmitter();

  @ViewChild('connectorIconImg') connectorIconImg: ElementRef;
  @ViewChild('connectorIconInput') connectorIconInput: ElementRef;

  createMode: boolean;
  editMode: boolean;
  apiConnectorDataForm: FormGroup;
  iconFile: File;
  originalValue = {};

  constructor(private formBuilder: FormBuilder) {}

  get processingError(): string {
    return this.apiConnectorState.hasErrors
      ? this.apiConnectorState.errors[0].message
      : null;
  }

  ngOnInit() {
    this.apiConnectorDataForm = this.formBuilder.group({
      name: ['', Validators.required],
      description: [''],
      host: [''],
      basePath: [''],
      icon: ['']
    });

    // If no particular connector is injected but there's a custom connector create
    // request in progress, we set the component in CREATE mode (inputs visible by default).
    if (!this.apiConnectorData && this.apiConnectorState.createRequest.name) {
      this.createMode = true;
      this.apiConnectorData = this.apiConnectorState.createRequest;
    } else if (!this.apiConnectorData) {
      throw new Error(
        `ApiConnectorInfoComponent requires either an ApiConnectorData object or an active custom connector create request`
      );
    }

    if (this.apiConnectorData) {
      const {
        name,
        description,
        configuredProperties,
        properties
      } = this.apiConnectorData;
      this.apiConnectorDataForm.get('name').setValue(name);
      this.apiConnectorDataForm.get('description').setValue(description);
      this.apiConnectorDataForm.get('host').setValue(configuredProperties.host || properties.host.defaultValue);
      this.apiConnectorDataForm.get('basePath').setValue(configuredProperties.basePath || properties.basePath.defaultValue);
      this.originalValue = this.apiConnectorDataForm.value;
    }

    if (!this.createMode) {
      this.apiConnectorDataForm.disable();
    }
  }

  editForm() {
    this.enableEdit();
  }

  enableEdit() {
    this.editMode = true;
    this.apiConnectorDataForm.enable();
  }

  disableEdit() {
    this.editMode = false;
    this.apiConnectorDataForm.disable();
  }

  cancelEdit() {
    this.disableEdit();
    this.clearValue(this.connectorIconInput);
    this.iconFile = undefined;
    this.apiConnectorDataForm.setValue(this.originalValue);
  }

  clearValue(el) {
    el.nativeElement.value = '';
  }

  onChange() {
    if (this.connectorIconInput.nativeElement.files) {
      const fileList = this.connectorIconInput.nativeElement.files as FileList;
      if (fileList.length > 0) {
        this.iconFile = fileList[0];
      }
    }
  }

  onSubmit(): void {
    if (this.apiConnectorDataForm.valid) {
      if ((!this.createMode && this.apiConnectorDataForm.dirty) || this.createMode) {
        const { name, description, host, basePath } = this.apiConnectorDataForm.value;
        const apiConnectorData = {
          ...this.apiConnectorData,
          configuredProperties: {
            ...this.apiConnectorData.configuredProperties,
            host,
            basePath,
          },
          name,
          description,
          iconFile: this.iconFile
        } as CustomConnectorRequest;

        this.update.emit(apiConnectorData);
        this.clearValue(this.connectorIconInput);
      }
      this.disableEdit();
    }
  }
}
