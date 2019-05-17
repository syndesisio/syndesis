import {
  Component,
  ViewChild,
  OnInit,
  OnDestroy,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { ApiDefinition, ApiEditorComponent } from 'apicurio-design-studio';
import * as YAML from 'js-yaml';
import { WindowRef } from './WindowRef';

@Component({
  selector: 'app-apicurio-host',
  template: `
    <api-editor
      #apicurioComponent
      [api]="apiDefinition"
      [embedded]="true"
      (onCommandExecuted)="onChanges()"
    >
    </api-editor>
  `,
})
export class ApicurioHostComponent implements OnInit, OnDestroy, OnChanges {
  @Input() title: string;
  @Input('specification')
  set specification(spec: string) {
    this.apiDefinition.spec = JSON.parse(spec);
  }
  @Output() onSpecification = new EventEmitter<ApiDefinition>();
  @ViewChild('apicurioComponent')
  private apicurioComponent: ApiEditorComponent;
  apiDefinition = new ApiDefinition();

  constructor(private winRef: WindowRef) {
    this.winRef.nativeWindow.dump = YAML.dump;
  }

  ngOnInit() {
    this.reset();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.reset();
  }

  reset() {
    this.apiDefinition.createdBy = 'user1';
    this.apiDefinition.createdOn = new Date();
    this.apiDefinition.tags = [];
    this.apiDefinition.description = '';
    this.apiDefinition.id = 'api-1';
  }

  ngOnDestroy() {}

  onChanges(): void {
    const value = this.apicurioComponent.getValue();
    this.onSpecification.emit(value);
  }
}
