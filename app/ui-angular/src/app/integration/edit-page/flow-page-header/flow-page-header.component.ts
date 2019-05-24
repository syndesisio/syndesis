import { Component } from '@angular/core';
import { CurrentFlowService } from '../current-flow.service';
import { FlowPageService } from '../flow-page.service';

@Component({
  selector: 'syndesis-integration-flow-page-header',
  templateUrl: './flow-page-header.component.html',
  styleUrls: [
    '../../integration-common.scss',
    './flow-page-header.component.scss'
  ]
})
export class FlowPageHeaderComponent {
  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService
  ) {}
}
