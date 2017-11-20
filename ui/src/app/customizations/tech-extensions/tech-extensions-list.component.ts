import { Component, OnInit } from '@angular/core';
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
import { Extensions } from '../../model';

@Component({
  selector: 'syndesis-tech-extensions-list',
  templateUrl: 'tech-extensions-list.component.html'
})
export class TechExtensionsListComponent implements OnInit {
  extensions: Observable<Extensions>;
  filteredExtensions: Subject<Extensions> = new BehaviorSubject(<Extensions>{});
  loading: Observable<boolean>;
  listConfig: ListConfig;

  constructor(private store: ExtensionStore) {
    this.extensions = this.store.list;
    this.loading = this.store.loading;
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
              tooltip: 'Import Technical Extension'
            }
          ],
          moreActions: []
        } as ActionConfig
      } as EmptyStateConfig
    };
  }

  ngOnInit() {
    this.store.loadAll();
  }

}
