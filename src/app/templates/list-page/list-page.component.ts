import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { TemplateStore } from '../../store/template/template.store';
import { Templates } from '../../store/template/template.model';

@Component({
  selector: 'ipaas-templates-list-page',
  templateUrl: './list-page.component.html',
  styleUrls: ['./list-page.component.scss'],
})
export class TemplatesListPage implements OnInit {

  templates: Observable<Templates>;
  loading: Observable<boolean>;

  constructor(private store: TemplateStore) {
    this.templates = this.store.list;
    this.loading = this.store.loading;
  }

  ngOnInit() {
    this.store.loadAll();
  }

}
