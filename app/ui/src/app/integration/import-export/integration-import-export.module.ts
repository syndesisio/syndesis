import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FileUploadModule } from 'ng2-file-upload';
import { SyndesisCommonModule, PatternflyUIModule } from '@syndesis/ui/common';

import { IntegrationImportModule, IntegrationImportComponent } from './import';
import { IntegrationImportExportComponent } from './integration-import-export.component';

const routes: Routes = [
  {
    path: '',
    component: IntegrationImportExportComponent,
    children: [
      {
        path: 'import',
        component: IntegrationImportComponent
      },
      {
        path: '',
        redirectTo: 'import-export'
      }
    ]
  }
];

@NgModule({
  imports: [
    CommonModule,
    SyndesisCommonModule,
    PatternflyUIModule,
    FileUploadModule,
    IntegrationImportModule,
    RouterModule.forChild(routes),
  ],
  exports: [RouterModule],
  declarations: [
    IntegrationImportExportComponent
  ]
})
export class IntegrationImportExportModule { }
