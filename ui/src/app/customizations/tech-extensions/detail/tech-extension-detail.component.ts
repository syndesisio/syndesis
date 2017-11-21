import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { Extension } from '../../../model';
import { ExtensionStore } from '../../../store/extension/extension.store';

@Component({
  selector: 'syndesis-tech-extension-detail',
  templateUrl: 'tech-extension-detail.component.html'
})
export class TechExtensionDetailComponent implements OnInit, OnDestroy {
  extension: Extension;
  loading: Observable<boolean>;
  subscription: Subscription;
  constructor(private store: ExtensionStore,
              private router: Router,
              private route: ActivatedRoute) {
    this.loading = store.loading;
               }

  ngOnInit() {
    this.subscription = this.store.resource.subscribe(extension => {
      this.extension = extension;
    });
    this.route.paramMap.first(params => params.has('id')).subscribe(params => this.store.load(params.get('id')));
   }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}
