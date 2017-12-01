import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ExtensionStore } from '../../store/extension/extension.store';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import {
  Action,
  ActionConfig,
  ListConfig,
  ListEvent,
  EmptyStateConfig,
} from 'patternfly-ng';
import { TechExtensionDeleteModalComponent } from './tech-extension-delete-modal.component';
import { Extensions, Extension } from '../../model';

@Component({
  selector: 'syndesis-tech-extensions-list',
  templateUrl: 'tech-extensions-list.component.html',
  styleUrls: ['./tech-extensions-list.component.scss']
})
export class TechExtensionsListComponent implements OnInit {
  extensions$: Observable<Extensions>;
  filteredExtensions$: Subject<Extensions> = new BehaviorSubject(<Extensions>{});
  loading$: Observable<boolean>;
  listConfig: ListConfig;
  @ViewChild(TechExtensionDeleteModalComponent)
  deleteModal: TechExtensionDeleteModalComponent;
  itemUseMapping: { [valueComparator: string]: string } = {
    '=0': '<strong>0</strong> integrations',
    '=1': '<strong>1</strong> integrations',
    'other': '<strong>#</strong> integrations'
  };

  constructor(private store: ExtensionStore,
              private router: Router,
              private route: ActivatedRoute) {
    this.extensions$ = this.store.list;
    this.loading$ = this.store.loading;
    this.listConfig = {
      dblClick: false,
      multiSelect: false,
      selectItems: false,
      showCheckbox: false,
      emptyStateConfig: {
        iconStyleClass: 'pficon pficon-add-circle-o',
        title: 'Import Technical Extension',
        info:
          'There are no technical extensions available',
        actions: {
          primaryActions: [
            {
              id: 'importTechnicalExtension',
              title: 'Import Technical Extension',
              tooltip: 'Import Technical Extension',
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
