import { Component, TemplateRef, Input, OnInit } from '@angular/core';

@Component({
  selector: 'syndesis-list-view-rows',
  templateUrl: 'list-view-rows.component.html',
})
export class ListViewRowsComponent implements OnInit {
  @Input()
  public items: Array<any>;
  @Input()
  public template: TemplateRef<any>;

  constructor() {}

  ngOnInit() { }

}
