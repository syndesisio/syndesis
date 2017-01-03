import { Component, OnInit } from '@angular/core';

import { Observable } from 'rxjs/Observable';
import { Store } from '@ngrx/store';

import { State, getTemplates } from '../store/store';
import { Templates } from '../store/template/template.model';

@Component({
  selector: 'ipaas-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {

  templates: Observable<Templates>;

  constructor(private store: Store<State>) { }

  ngOnInit() {
    this.templates = this.store.select(getTemplates);
  }

}
