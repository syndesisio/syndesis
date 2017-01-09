import { Component, OnInit } from '@angular/core';

import { Observable } from 'rxjs/Observable';

import { TemplateStore } from '../store/template/template.store';
import { Templates } from '../store/template/template.model';

@Component({
  selector: 'ipaas-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {

  templates: Observable<Templates>;

  constructor(private store: TemplateStore) {
    this.templates = this.store.list;
  }

  ngOnInit() {
    this.store.loadAll();
  }

}
