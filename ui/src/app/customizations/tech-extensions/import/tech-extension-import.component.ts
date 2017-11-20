import { Component, OnInit } from '@angular/core';
import { ExtensionStore } from '../../../store/extension/extension.store';

@Component({
  selector: 'syndesis-tech-extentions-import',
  templateUrl: 'tech-extension-import.component.html'
})

export class TechExtensionImportComponent implements OnInit {
  constructor(private store: ExtensionStore) { }

  ngOnInit() {
    // WIP
  }
}
