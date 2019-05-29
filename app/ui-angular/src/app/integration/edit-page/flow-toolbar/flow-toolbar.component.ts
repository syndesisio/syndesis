import { Component, ElementRef, ViewChild, Input, OnInit } from '@angular/core';
import { CurrentFlowService } from '../current-flow.service';
import { FlowPageService } from '../flow-page.service';
import { ActivatedRoute } from '@angular/router';
import { INTEGRATION_SET_PROPERTY } from '../edit-page.models';
import { Flow, ALTERNATE, PRIMARY } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-integration-flow-toolbar',
  templateUrl: './flow-toolbar.component.html',
  styleUrls: ['../../integration-common.scss', './flow-toolbar.component.scss'],
})
export class FlowToolbarComponent implements OnInit {
  @Input() hideButtons = false;
  @ViewChild('nameInput') nameInput: ElementRef;
  @Input()
  flows: Flow[] = [];
  @Input()
  currentFlow: Flow = undefined;

  private targetUrl: string;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.route.queryParamMap.subscribe(params => this.targetUrl = params.get('targetUrl'));
  }

  get saveInProgress() {
    return this.flowPageService.saveInProgress;
  }

  get publishInProgress() {
    return this.flowPageService.publishInProgress;
  }

  nameUpdated(name: string) {
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SET_PROPERTY,
      property: 'name',
      value: name,
    });
  }

  flowNameUpdated(name: string) {
    this.currentFlow.name = name;
  }

  save(targetRoute: string[]) {
    this.flowPageService.save(
      this.route.firstChild,
      targetRoute || ['..', 'save-or-add-step'], this.targetUrl
    );
  }

  getFlowName(flow: Flow) {
    if (this.isConditionalFlow(flow)) {
      return flow.name || 'Conditional Flow';
    }

    if (this.isDefaultFlow(flow)) {
      return flow.name || 'Default Flow';
    }

    return 'Flow';
  }

  isPrimaryFlow(flow: Flow) {
    return !flow.type || flow.type === PRIMARY;
  }

  isAlternateFlow(flow: Flow) {
    return flow.type && flow.type === ALTERNATE;
  }

  isConditionalFlow(flow: Flow) {
    return this.isAlternateFlow(flow) && flow.metadata['kind'] === 'conditional';
  }

  isDefaultFlow(flow: Flow) {
    return this.isAlternateFlow(flow) && flow.metadata['kind'] === 'default';
  }

  getConditionalFlowGroups() {
    const conditionalFlows = this.flows.filter(flow => this.isConditionalFlow(flow));

    // Add default flows to the very end of the list, ensures that default flows are always at the end of a group
    conditionalFlows.push(...this.flows.filter(flow => this.isDefaultFlow(flow)));

    // potentially we have many flows that belong to different steps, so group flows by step id
    const flowGroups = [];
    conditionalFlows.forEach(flow => {
      const stepId = flow.metadata['stepId'];
      const flowGroup = flowGroups.find(group => group.id === stepId);
      if (flowGroup) {
        flowGroup['flows'].push(flow);
      } else {
        flowGroups.push({id: stepId, flows: [flow]});
      }
    });

    return flowGroups;
  }

  publish() {
    this.flowPageService.publish(this.route.firstChild);
  }

  get currentStep() {
    return this.flowPageService.getCurrentStep(this.route);
  }
}
