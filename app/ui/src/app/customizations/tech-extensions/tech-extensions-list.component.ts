import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ExtensionStore } from '@syndesis/ui/store/extension/extension.store';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import {
  ActionConfig,
  ListConfig,
  EmptyStateConfig
} from 'patternfly-ng';
import { ConfigService } from '@syndesis/ui/config.service';
import { TechExtensionDeleteModalComponent } from '@syndesis/ui/customizations/tech-extensions/tech-extension-delete-modal.component';
import { Extensions, I18NService } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-tech-extensions-list',
  templateUrl: 'tech-extensions-list.component.html',
  styleUrls: ['./tech-extensions-list.component.scss']
})
export class TechExtensionsListComponent implements OnInit {
  extensions$: Observable<Extensions>;
  filteredExtensions$: Subject<Extensions> = new BehaviorSubject(
    <Extensions>{}
  );
  loading$: Observable<boolean>;
  listConfig: ListConfig;
  @ViewChild(TechExtensionDeleteModalComponent)
  deleteModal: TechExtensionDeleteModalComponent;

  constructor(
    private store: ExtensionStore,
    public config: ConfigService,
    private router: Router,
    private route: ActivatedRoute,
    private i18nService: I18NService
  ) {
    this.extensions$ = this.store.list;
    this.loading$ = this.store.loading;
    this.listConfig = {
      dblClick: false,
      multiSelect: false,
      selectItems: false,
      showCheckbox: false,
      emptyStateConfig: {
        iconStyleClass: 'pficon pficon-add-circle-o',
        title: this.i18nService.localize( 'customizations.extensions.import-extension' ),
        info: this.i18nService.localize( 'customizations.extensions.no-extensions-message' ),
        actions: {
          primaryActions: [
            {
              id: 'importTechnicalExtension',
              title: this.i18nService.localize( 'customizations.extensions.import-extension' ),
              tooltip: this.i18nService.localize( 'customizations.extensions.import-extension' ),
              visible: true,
              disabled: false
            }
          ],
          moreActions: []
        } as ActionConfig
      } as EmptyStateConfig
    };
  }

  handleAction(event: any) {
    if (event.id === 'importTechnicalExtension') {
      this.router.navigate(['import'], { relativeTo: this.route });
    }
  }

  handleClick(event: any) {
    const extension = event.item;
    this.router.navigate([extension.id], { relativeTo: this.route });
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
    this.store.loadAll();
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
