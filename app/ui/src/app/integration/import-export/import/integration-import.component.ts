import { Component, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { PlatformState } from '@syndesis/ui/platform';


import {
  IntegrationImportUpload
} from './integration-import.actions';

@Component({
  selector: 'syndesis-import-integration-component',
  templateUrl: './integration-import.component.html'
})
export class IntegrationImportComponent implements OnInit {
  constructor(private store: Store<PlatformState>) { }

  ngOnInit() {
    const payload = {
      list: [],
      file: null,
      importResults: {
        integrations: [],
        connections: []
      }
    };

    this.store.dispatch(new IntegrationImportUpload(payload));
  }
}
