import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { ApiDefinition, ApiEditorComponent } from 'apicurio-design-studio';
import * as YAML from 'yamljs';
import { WindowRef } from '@syndesis/ui/customizations/window-ref';

@Component({
  selector: 'openapi-editor, [openapi-editor]',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.scss']
})
export class OpenApiEditorComponent implements OnInit {
  @Input() title: string;
  @Input('specification')
  set specification(spec: string) {
    this.apiDefinition.spec = JSON.parse(spec);
  }
  @Output() onSave = new EventEmitter<ApiDefinition>();
  @Output() onCancel = new EventEmitter<boolean>();
  @Output() onBack = new EventEmitter<boolean>();

  @ViewChild('_apiEditor') _apiEditor: ApiEditorComponent;
  apiDefinition = new ApiDefinition();
  editorHasChanges = false;

  constructor(
    private winRef: WindowRef
  ) {
    this.winRef.nativeWindow.dump = YAML.dump;
  }

  ngOnInit(): void {
    this.apiDefinition.createdBy = 'user1';
    this.apiDefinition.createdOn = new Date();
    this.apiDefinition.tags = [];
    this.apiDefinition.description = '';
    this.apiDefinition.id = 'api-1';
  }

  onDoneEditing(): void {
    const value = this._apiEditor.getValue();
    this.onSave.emit(value);
  }

  onChanges(): void {
    this.editorHasChanges = true;
  }
}
