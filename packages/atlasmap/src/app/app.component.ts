import { Component } from '@angular/core';
import {
  InitializationService,
  ErrorHandlerService,
  DocumentManagementService,
  MappingManagementService,
} from '@atlasmap/atlasmap-data-mapper';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  providers: [
    InitializationService,
    MappingManagementService,
    ErrorHandlerService,
    DocumentManagementService,
  ],
})
export class AppComponent {
  title = 'atlasmap';
}
