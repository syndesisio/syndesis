import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ExtensionStore } from '@syndesis/ui/store/extension/extension.store';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import {
  Action,
  ActionConfig,
  ListConfig,
  ListEvent,
  EmptyStateConfig
} from 'patternfly-ng';
import { ConfigService } from '@syndesis/ui/config.service';
import { TechExtensionDeleteModalComponent } from '@syndesis/ui/customizations/tech-extensions/tech-extension-delete-modal.component';
import { Extensions, Extension } from '@syndesis/ui/platform';

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
  itemUseMapping: { [valueComparator: string]: string } = {
    '=1': '<strong>1</strong> integration',
    other: '<strong>#</strong> integrations'
  };

  constructor(
    private store: ExtensionStore,
    public config: ConfigService,
    private router: Router,
    private route: ActivatedRoute
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
        title: 'Import Extension',
        info: 'There are no extensions available.',
        actions: {
          primaryActions: [
            {
              id: 'importTechnicalExtension',
              title: 'Import Extension',
              tooltip: 'Import Extension',
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

  ngOnInit() {
    this.store.loadAll();
  }
}
