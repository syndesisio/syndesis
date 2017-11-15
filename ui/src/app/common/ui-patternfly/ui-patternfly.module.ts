import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TextMaskModule } from 'angular2-text-mask';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { ToolbarModule } from 'patternfly-ng';
import { SyndesisFormComponent } from './syndesis-form-control.component';
import { ListToolbarComponent } from './list-toolbar/list-toolbar.component';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TextMaskModule,
    DynamicFormsCoreModule,
    TooltipModule.forRoot(),
    RouterModule,
    ToolbarModule
  ],
  declarations: [SyndesisFormComponent, ListToolbarComponent],
  exports: [DynamicFormsCoreModule, SyndesisFormComponent, ListToolbarComponent]
})
export class PatternflyUIModule {}
