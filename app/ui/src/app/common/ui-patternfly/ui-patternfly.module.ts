import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TextMaskModule } from 'angular2-text-mask';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import { TooltipModule } from 'ngx-bootstrap';
import { ToolbarModule, CardModule, ListModule } from 'patternfly-ng';
import { SyndesisFormComponent } from './syndesis-form-control.component';
import { DurationFormControlComponent } from './duration-form-control.component';
import { ListToolbarComponent } from './list-toolbar/list-toolbar.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TextMaskModule,
    DynamicFormsCoreModule,
    TooltipModule.forRoot(),
    RouterModule,
    ToolbarModule,
    CardModule,
    ListModule,
  ],
  declarations: [
    SyndesisFormComponent,
    DurationFormControlComponent,
    ListToolbarComponent
  ],
  exports: [
    DurationFormControlComponent,
    DynamicFormsCoreModule,
    SyndesisFormComponent,
    ListToolbarComponent,
    ToolbarModule,
    CardModule,
    ListModule
  ]
})
export class PatternflyUIModule {}
