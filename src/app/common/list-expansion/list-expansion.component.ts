import { Component, TemplateRef, Input, OnInit } from '@angular/core';

export interface Expandable {
  expanded: boolean;
}

@Component({
  selector: 'syndesis-list-expansion',
  templateUrl: 'list-expansion.component.html',
})
export class ListExpansionComponent implements OnInit {
  @Input() public items: Array<Expandable>;
  @Input() public mainTemplate: TemplateRef<any>;
  @Input() public expansionTemplate: TemplateRef<any>;

  constructor() {}

  ngOnInit() {}
}
