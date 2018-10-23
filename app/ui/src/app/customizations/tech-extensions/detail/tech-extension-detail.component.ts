import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { first } from 'rxjs/operators';
import { Extension, I18NService, Integrations } from '@syndesis/ui/platform';
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
