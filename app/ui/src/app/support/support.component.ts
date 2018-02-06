import { Component,  Input,  ViewChild, ElementRef, OnInit } from '@angular/core';

import { ObjectPropertyFilterConfig } from '../common/object-property-filter.pipe';
import { ObjectPropertySortConfig } from '../common/object-property-sort.pipe';

import { Restangular } from 'ngx-restangular';
import { Http } from '@angular/http';

import { Integrations, Integration } from '../platform/types/integration/integration.model';
import { IntegrationStore } from '../store/integration/integration.store';
import { IntegrationSupportProviderService } from '../integration/integration-support-provider.service';
import { log, getCategory } from '../logging';
import { Observable } from 'rxjs/Observable';

import fileSaver = require('file-saver');

const ARCHIVE_FILE_NAME = 'syndesis.zip';

@Component({
  selector: 'syndesis-support',
  templateUrl: './support.component.html',
  styleUrls: ['./support.component.scss']
})
export class SupportComponent implements OnInit {

  allLogsSelected = true;
  loading = true;

  filter: ObjectPropertyFilterConfig = {
    filter: '',
    propertyName: 'name'
  };
  sort: ObjectPropertySortConfig = {
    sortField: 'name',
    descending: false
  };
    
  // List configuration
  listConfig = {
    multiSelect: true,
    selectItems: false,
    showCheckbox: true
  };

  // Toolbar configuration
  toolbarConfig = {
    filterConfig: {
      fields: [
        {
          id: 'name',
          title: 'Name',
          placeholder: 'Filter by Name...',
          type: 'text'
        },
        {
          id: 'description',
          title: 'Description',
          placeholder: 'Filter by Description...',
          type: 'text'
        }
      ]
    },
    sortConfig: {
      fields: [
        {
          id: 'name',
          title: 'Name',
          sortType: 'alpha'
        }
      ],
      isAscending: true
    }
  };

  items: Array<any> = [
    {
        name: 'Integration Name 1',
        description: 'Description about the integration goes here'
    },
    {
        name: 'Integration Name 2',
        description: 'Description about the integration goes here'
    }
  ];
  private restangularService: Restangular;

  constructor(
    public store: IntegrationStore,
    public integrationSupportService: IntegrationSupportProviderService,
  ) {
  }

  buildData(data: any = {}) {
    this.integrationSupportService
      .downloadSupportData(data)
      .subscribe(response => {
        fileSaver.saveAs(response, ARCHIVE_FILE_NAME);
      },
      error => {
        /*
        console.log('Error downloading the file.');
        console.log(error);
        */
        // TODO properly report this
      }
    );
    return {  };
  }

  // Handles events when the user interacts with the toolbar filter
  filterChanged($event) {
    // TODO update our pipe to handle multiple filters
    if ($event.appliedFilters.length === 0) {
      this.filter.filter = '';
    }
    $event.appliedFilters.forEach(filter => {
      this.filter.propertyName = filter.field.id;
      this.filter.filter = filter.value;
    });
  }

  // Handles events when the user interacts with the toolbar sort
  sortChanged($event) {
    this.sort.sortField = $event.field.id;
    this.sort.descending = !$event.isAscending;
  }

  onSubmit() {
    let chosen = [];
    if (this.allLogsSelected) {
      chosen = this.items;
    } else {
      chosen = this.items.filter(x => x.selected === true);
    }
    const input = {};
    chosen.forEach(el => input[el.name] = true);
    this.buildData(input);
  }

  deselectAll() {
    this.items.forEach(item => item.selected = false);
  }

  handleSelectionChange(event) {
    this.allLogsSelected = false;
  }

  totalItems(): number {
    return this.items.length;
  }

  selectedItems(): number {
    return this.items.filter(x => x.selected === true).length;
  }

  public ngOnInit() {
    this.store.list.subscribe(integration  => {
      this.items = integration as Integration[];
    });
    this.store.loadAll();
  }

}
