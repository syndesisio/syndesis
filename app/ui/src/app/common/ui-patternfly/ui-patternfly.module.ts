import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TextMaskModule } from 'angular2-text-mask';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import { SyndesisFormComponent } from './syndesis-form-control.component';
import { DurationFormControlComponent } from './duration-form-control.component';
import { ListToolbarComponent } from './list-toolbar/list-toolbar.component';
import { VendorModule } from '@syndesis/ui/vendor';

@NgModule({
  imports: [
    VendorModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TextMaskModule,
    DynamicFormsCoreModule,
    RouterModule,
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
  ]
})
export class PatternflyUIModule {}
