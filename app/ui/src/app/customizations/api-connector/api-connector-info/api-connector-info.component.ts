import {
  Component,
  OnInit,
  Input,
  Output,
  EventEmitter,
  Renderer2,
  HostListener
} from '@angular/core';
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

  createMode: boolean;
  apiConnectorDataForm: FormGroup;
  editControlKey: string;
  iconFile: File;
  private isDirty: boolean;

  constructor(private formBuilder: FormBuilder, private renderer: Renderer2) {}

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
      basePath: ['']
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
      this.apiConnectorDataForm
        .get('host')
        .setValue(configuredProperties.host || properties.host.defaultValue);
      this.apiConnectorDataForm
        .get('basePath')
        .setValue(
          configuredProperties.basePath || properties.basePath.defaultValue
        );
      this.isDirty = true;
    }
  }

  onChange(event): void {
    if (event.target && event.target.files) {
      const fileList = event.target.files as FileList;
      if (fileList.length > 0) {
        this.iconFile = fileList[0];
      }
    }
    this.isDirty = true;
    // If component is not in "Create" mode (eg. detail page), updating
    // any input field will automatically fire up the submit handler
    if (!this.createMode) {
      this.onSubmit();
    }
  }

  onSubmit(): void {
    if (this.apiConnectorDataForm.valid && this.isDirty) {
      const {
        name,
        description,
        host,
        basePath
      } = this.apiConnectorDataForm.value;
      const apiConnectorData = {
        ...this.apiConnectorData,
        configuredProperties: {
          ...this.apiConnectorData.configuredProperties,
          host,
          basePath
        },
        name,
        description,
        iconFile: this.iconFile
      } as CustomConnectorRequest;

      this.update.emit(apiConnectorData);
    }
    this.editControlKey = null;
  }

  onEditEnable(event: Event, key: string): void {
    event.preventDefault();
    event.stopPropagation();
    this.editControlKey = key;
    this.isDirty = false;
    setTimeout(() => this.renderer.selectRootElement(`#${key}`).focus(), 0);
  }

  onEditChange(): void {
    this.isDirty = true;
  }

  @HostListener('document:click', ['$event'])
  private onDocumentClick(event: Event): void {
    const element: any = event.target;
    const elementTag = element.tagName.toLowerCase();
    if (
      elementTag !== 'input' &&
      elementTag !== 'textarea' &&
      this.editControlKey
    ) {
      setTimeout(() => this.onSubmit(), 0);
    }
  }
}
