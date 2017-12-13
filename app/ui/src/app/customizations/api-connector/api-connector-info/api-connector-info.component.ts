import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import { ApiConnectorData } from '@syndesis/ui/customizations/api-connector';

@Component({
  selector: 'syndesis-api-connector-info',
  templateUrl: './api-connector-info.component.html',
  styleUrls: ['./api-connector-info.component.scss']
})
export class ApiConnectorInfoComponent implements OnInit {
  @Input() enableEditing: boolean;
  @Input() apiConnectorData: ApiConnectorData;
  @Output() update = new EventEmitter<ApiConnectorData>();

  apiConnectorInfoForm: FormGroup;
  icon: string; // TODO - Replace default thumb by image if any. Wrap in square container
  private iconFile: File;

  constructor(private formBuilder: FormBuilder) {}

  ngOnInit() {
    this.apiConnectorInfoForm = this.formBuilder.group({
      name: ['', Validators.required],
      description: [''],
      host: [''],
      baseUrl: [''],
    });

    if (this.apiConnectorData) {
      const { name, description, host, baseUrl, icon } = this.apiConnectorInfoForm.value;
      this.apiConnectorInfoForm.get('name').setValue(name);
      this.apiConnectorInfoForm.get('description').setValue(description);
      this.apiConnectorInfoForm.get('host').setValue(host);
      this.apiConnectorInfoForm.get('baseUrl').setValue(baseUrl);
      this.icon = icon;
    }
  }

  onChange(event): void {
    if (event.target && event.target.files) {
      const fileList = event.target.files as FileList;
      if (fileList.length > 0) {
        this.iconFile = fileList.item[0];
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
      const { name, description, host, baseUrl } = this.apiConnectorInfoForm.value;
      const apiConnectorData = {
        ...this.apiConnectorData,
        name,
        description,
        host,
        baseUrl,
        iconFile: this.iconFile
      } as ApiConnectorData;

      this.update.emit(apiConnectorData);
    }
  }
}
