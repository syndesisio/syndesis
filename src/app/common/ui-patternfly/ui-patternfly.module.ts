import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { TextMaskModule } from 'angular2-text-mask';
import { DynamicFormsCoreModule } from '@ng2-dynamic-forms/core';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { SyndesisFormComponent } from './syndesis-form-control.component';

@NgModule({

    imports: [
        CommonModule,
        ReactiveFormsModule,
        TextMaskModule,
        DynamicFormsCoreModule,
        TooltipModule.forRoot(),
    ],
    declarations: [
        SyndesisFormComponent,
    ],
    exports: [
        DynamicFormsCoreModule,
        SyndesisFormComponent,
    ],
})

export class DynamicFormsPatternflyUIModule {
}
