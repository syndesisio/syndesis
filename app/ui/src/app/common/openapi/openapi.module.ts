import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApicurioCommonComponentsModule, ApicurioEditorModule } from 'apicurio-design-studio';
import { FileUploadModule } from 'ng2-file-upload';

import { SyndesisCommonModule } from '@syndesis/ui/common';
import { VendorModule } from '@syndesis/ui/vendor';
import { OpenApiReviewComponent } from '@syndesis/ui/common/openapi/review/review.component';
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
    VendorModule
  ],
  declarations: [
    OpenApiReviewComponent,
    OpenApiEditorComponent,
    OpenApiUploaderComponent
  ],
  exports: [
    OpenApiReviewComponent,
    OpenApiEditorComponent,
    OpenApiUploaderComponent
  ]
})
export class OpenApiModule { }
