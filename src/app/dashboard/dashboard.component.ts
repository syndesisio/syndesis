import { Component, OnInit, ChangeDetectorRef } from '@angular/core';

import { Restangular } from 'ng2-restangular';

@Component({
  selector: 'ipaas-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  errorMessage: string;

  templates: any[] = [];

  constructor(private restangular: Restangular) { }

  ngOnInit() {
    this.restangular.all('integrationtemplates').getList().subscribe(
      (templates) => {
        this.templates = templates;
      },
      (error) => this.errorMessage = <any>error
    );
  }

}
