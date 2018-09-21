import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApicurioCommonComponentsModule, ApicurioEditorModule } from 'apicurio-design-studio';
import { FileUploadModule } from 'ng2-file-upload';

import { SyndesisCommonModule } from '@syndesis/ui/common';
import { OpenApiValidatorComponent } from '@syndesis/ui/common/openapi/validator/validator.component';
import { OpenApiEditorComponent } from '@syndesis/ui/common/openapi/editor/editor.component';
import { OpenApiUploaderComponent } from '@syndesis/ui/common/openapi/uploader/uploader.component';

@NgModule({
  imports: [
    CommonModule,
    SyndesisCommonModule,
    ApicurioEditorModule,
    ApicurioCommonComponentsModule,
    FormsModule,
    FileUploadModule,
  ],
  declarations: [
    OpenApiValidatorComponent,
    OpenApiEditorComponent,
    OpenApiUploaderComponent
  ],
  exports: [
    OpenApiValidatorComponent,
    OpenApiEditorComponent,
    OpenApiUploaderComponent
  ]
})
export class OpenApiModule { }
