import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Integration } from '../../../store/integration/integration.model';

@Component({
  selector: 'ipaas-integrations-flow-view',
  templateUrl: './flow-view.component.html',
  styleUrls: ['./flow-view.component.scss']
})
export class FlowViewComponent implements OnInit {

  @Input() integration: Observable<Integration>;
  i: Integration;

  ngOnInit() {
    this.integration.subscribe((integration) => {
      this.i = integration;
    });
  }

}