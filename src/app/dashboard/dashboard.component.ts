import { Component, OnInit } from '@angular/core';

import { Restangular } from 'ng2-restangular';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  constructor(private restangular: Restangular) { }

  ngOnInit() {
    this.restangular.all('connections').getList();
  }

}
