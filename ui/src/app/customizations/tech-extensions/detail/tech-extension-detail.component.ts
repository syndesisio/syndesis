import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Extension } from '../../../model';
import { ExtensionStore } from '../../../store/extension/extension.store';

@Component({
  selector: 'syndesis-tech-extension-detail',
  templateUrl: 'tech-extension-detail.component.html'
})
export class TechExtensionDetailComponent implements OnInit {
  extension$: Observable<Extension>;
  loading$: Observable<boolean>;
  constructor(private extensionStore: ExtensionStore,
              private router: Router,
              private route: ActivatedRoute) {
    this.loading$ = this.extensionStore.loading;
    this.extension$ = this.extensionStore.resource;
  }

  ngOnInit() {
    this.route.paramMap.first(params => params.has('id')).subscribe(params => this.extensionStore.load(params.get('id')));
   }
}
