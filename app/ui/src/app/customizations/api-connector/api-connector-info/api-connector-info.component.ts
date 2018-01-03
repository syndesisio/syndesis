import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import { CustomConnectorRequest, ApiConnectorState } from '@syndesis/ui/customizations/api-connector';

@Component({
  selector: 'syndesis-api-connector-info',
  templateUrl: './api-connector-info.component.html',
  styleUrls: ['./api-connector-info.component.scss']
})
export class ApiConnectorInfoComponent implements OnInit {
  @Input() enableEditing: boolean;
  @Input() apiConnectorState: ApiConnectorState;
  @Output() update = new EventEmitter<CustomConnectorRequest>();

  apiConnectorCreateRequest: CustomConnectorRequest;
  apiConnectorInfoForm: FormGroup;
  icon: string; // TODO - Replace default thumb by image if any. Wrap in square container
  private iconFile: File;

  constructor(private formBuilder: FormBuilder) { }

  get processingError(): string {
    return this.apiConnectorState.hasErrors ?
      this.apiConnectorState.errors[0].message : null;
  }

  ngOnInit() {
    this.apiConnectorInfoForm = this.formBuilder.group({
      name: ['', Validators.required],
      description: [''],
      host: [''],
      basePath: [''],
    });

    this.apiConnectorCreateRequest = this.apiConnectorState.createRequest;

    if (this.apiConnectorCreateRequest) {
      const { name, description, properties, icon } = this.apiConnectorCreateRequest;
      this.apiConnectorInfoForm.get('name').setValue(name);
      this.apiConnectorInfoForm.get('description').setValue(description);
      this.apiConnectorInfoForm.get('host').setValue(properties.host.defaultValue);
      this.apiConnectorInfoForm.get('basePath').setValue(properties.basePath.defaultValue);
      this.icon = icon;
    }
  }

  onChange(event): void {
    if (event.target && event.target.files) {
      const fileList = event.target.files as FileList;
      if (fileList.length > 0) {
        this.iconFile = fileList[0];
      }
    }
    // If component is in edit mode (eg. detail page), updating
    // any input field will automatically fire up the submit handler
    if (this.enableEditing) {
      this.onSubmit();
    }
  }

  onSubmit(): void {
    if (this.apiConnectorInfoForm.valid) {
      const { name, description, host, basePath } = this.apiConnectorInfoForm.value;
      const apiConnectorCreateRequest = {
        ...this.apiConnectorCreateRequest,
        configuredProperties: {
          ...this.apiConnectorCreateRequest.configuredProperties,
          host,
          basePath,
        },
        name,
        description,
        file: this.iconFile
      } as CustomConnectorRequest;

      this.update.emit(apiConnectorCreateRequest);
    }
  }
}
