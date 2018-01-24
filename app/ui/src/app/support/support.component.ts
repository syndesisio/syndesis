import { Component,  Input,  ViewChild, ElementRef } from '@angular/core';

import { ObjectPropertyFilterConfig } from '../common/object-property-filter.pipe';
import { ObjectPropertySortConfig } from '../common/object-property-sort.pipe';

import { Restangular } from 'ngx-restangular';
import { Http } from '@angular/http'

import { Integrations, Integration } from '../integration/integration.model';
import { IntegrationStore } from '../store/integration/integration.store';
import { IntegrationSupportService } from '../integration/integration-support.service';
import { log, getCategory } from '../logging';

import fileSaver = require("file-saver");

const SUPPORT_FORM_CONFIG = {
  platformLogs: {
    displayName: 'Platform Logs',
    type: 'boolean',
    description: 'Include logs of platform services',
    default: false
  },
  integrationLogs: {
    displayName: 'Integrations Logs',
    type: 'boolean',
    description: 'Include logs of integrations',
  }
};

@Component({
  selector: 'syndesis-support',
  templateUrl: './support.component.html',
  styleUrls: ['./support.component.scss']
})
export class SupportComponent {

  // Used to bind patternfly behavior with foreign form elements
  @ViewChild('alllogs') allogs: ElementRef;
  @ViewChild('specificlogs') specificlogs: ElementRef;

  allLogsSelected : boolean = true;
  loading : boolean= true;

  private restangularService: Restangular;

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
        name: "Integration Name 1",
        description: "Description about the integration goes here"
    },
    {
        name: "Integration Name 2",
        description: "Description about the integration goes here"
    }
  ];

  constructor(
    public store: IntegrationStore,
    public integrationSupportService: IntegrationSupportService,
  ) {}

  getPods() {
    this.integrationSupportService
      .getPods()
      .toPromise()
      .then((resp: any) => {
        const body = JSON.parse(resp['_body']);
        console.log('++++++++++++++++++++' + body );
      });
  }



  // loadForm() {
  //   //this.formConfig = SUPPORT_FORM_CONFIG;
  //   this.integrationSupportService
  //     .getSupportFormConfiguration()
  //     .toPromise()
  //     .then((resp: any) => {
  //       this.loading = false;
  //       this.formConfig = JSON.parse(resp['_body']);
  //       this.formModel = this.formFactory.createFormModel( this.formConfig, {} );
  //       // eventually customize form
  //       // this.formModel
  //       // .filter(model => model instanceof DynamicInputModel)
  //       // .forEach(model => ((<DynamicInputModel>model).readOnly = readOnly));
  //       this.formGroup = this.formService.createFormGroup(this.formModel);
  //     });
  // }
  
  buildData(data: Array<any>) {
    console.log(data);
    this.integrationSupportService
      .downloadSupportData(data)
      .map(res => res.blob())
      .subscribe(response => {
        fileSaver.saveAs(response, "syndesis.zip");
      },
      error => console.log("Error downloading the file.")
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
    if(this.allLogsSelected) {
      chosen = this.items;
    } else {
      chosen = this.items.filter(x => x.selected === true);
    }
    this.buildData(chosen);
  }

  deselectAll() {
    this.items.forEach(item => item.selected = false);
  }

  handleSelectionChange(event) {
    this.allLogsSelected = false;
  }

  totalItems() : number {
    return this.items.length;
  }

  selectedItems() : number {
    return this.items.filter(x => x.selected === true).length;
  }

}
