import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { first } from 'rxjs/operators';
import { Extension, I18NService, Integrations } from '@syndesis/ui/platform';
import { ExtensionStore } from '@syndesis/ui/store';
import { ExtensionDeleteModalComponent } from '@syndesis/ui/customizations/extensions';

@Component({
  selector: 'syndesis-extension-detail',
  templateUrl: 'extension-detail.component.html',
  styleUrls: [
    '../extension-common.scss',
    'extension-detail.component.scss'
  ]
})
export class ExtensionDetailComponent implements OnInit {
  extension$: Observable<Extension>;
  integrations$: Observable<Integrations>;
  loading$: Observable<boolean>;
  @ViewChild(ExtensionDeleteModalComponent)
  deleteModal: ExtensionDeleteModalComponent;

  constructor(
    private extensionStore: ExtensionStore,
    private route: ActivatedRoute,
    private i18nService: I18NService
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

  usageText( uses: number ): string {
    if ( uses === 1 ) {
      return this.i18nService.localize( 'customizations.extensions.used-once-msg' );
    }

    if ( uses > 1 ) {
      return this.i18nService.localize( 'customizations.extensions.used-multi-msg', [ uses ] );
    }

    return this.i18nService.localize( 'customizations.extensions.not-used-msg' );
  }
}
