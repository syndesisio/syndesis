import { Component, OnInit } from '@angular/core';
import { ExtensionStore } from '../../../store/extension/extension.store';

@Component({
  selector: 'syndesis-tech-extension-detail',
  templateUrl: 'tech-extension-detail.component.html'
})
export class TechExtensionDetailComponent implements OnInit {
  constructor(private store: ExtensionStore) { }

  ngOnInit() {
    // WIP
   }
}
