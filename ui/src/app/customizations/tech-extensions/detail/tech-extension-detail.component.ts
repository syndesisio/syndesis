import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Extension, Integrations } from '../../../model';
import { ExtensionStore } from '../../../store/extension/extension.store';
import { TechExtensionDeleteModalComponent } from 'app/customizations/tech-extensions';

@Component({
  selector: 'syndesis-tech-extension-detail',
  templateUrl: 'tech-extension-detail.component.html',
  styleUrls: ['tech-extension-detail.component.scss']

})
export class TechExtensionDetailComponent implements OnInit {
  extension$: Observable<Extension>;
  integrations$: Observable<Integrations>;
  loading$: Observable<boolean>;
  @ViewChild(TechExtensionDeleteModalComponent) deleteModal: TechExtensionDeleteModalComponent;

  constructor(private extensionStore: ExtensionStore,
              private router: Router,
              private route: ActivatedRoute) {
    this.loading$ = this.extensionStore.loading;
    this.extension$ = this.extensionStore.resource;
  }

  ngOnInit() {
    this.route.paramMap.first(params => params.has('id')).subscribe(params => {
      const id = params.get('id');
      this.extensionStore.load(id);
      this.integrations$ = this.extensionStore.loadIntegrations(id);
    });
   }
}
