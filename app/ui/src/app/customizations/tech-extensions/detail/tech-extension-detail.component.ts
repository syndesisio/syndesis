import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { first } from 'rxjs/operators';
import { Extension, Integrations } from '@syndesis/ui/platform';
import { ExtensionStore } from '@syndesis/ui/store';
import { TechExtensionDeleteModalComponent } from '@syndesis/ui/customizations/tech-extensions';

@Component({
  selector: 'syndesis-tech-extension-detail',
  templateUrl: 'tech-extension-detail.component.html',
  styleUrls: [
    '../tech-extension-common.scss',
    'tech-extension-detail.component.scss'
  ]
})
export class TechExtensionDetailComponent implements OnInit {
  extension$: Observable<Extension>;
  integrations$: Observable<Integrations>;
  loading$: Observable<boolean>;
  @ViewChild(TechExtensionDeleteModalComponent)
  deleteModal: TechExtensionDeleteModalComponent;
  integrationLengthMapping: any = {
    '=0': 'customizations.extensions.not-used-msg',
    '=1': 'customizations.extensions.used-once-msg',
    other: 'customizations.extensions.used-multi-msg'
  };

  constructor(
    private extensionStore: ExtensionStore,
    private route: ActivatedRoute
  ) {
    this.loading$ = this.extensionStore.loading;
    this.extension$ = this.extensionStore.resource;
  }

  /**
   * Indicates if the extension is being used in any integrations.
   *
   * @param extension the extension being checked
   * @returns {boolean} `true` if used in an integration
   */
  isBeingUsed( extension: any ): boolean {
    return extension.uses ? extension.uses > 0 : false;
  }

  ngOnInit() {
    this.route.paramMap
      .pipe(first(params => params.has('id')))
      .subscribe(params => {
        const id = params.get('id');
        this.extensionStore.load(id);
        this.integrations$ = this.extensionStore.loadIntegrations(id);
      });
  }
}
