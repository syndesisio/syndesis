import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { IntegrationStore } from '../../../store/integration/integration.store';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Integration, Step, TypeFactory } from '../../../model';

@Component({
  selector: 'ipaas-integrations-save-or-add-step',
  templateUrl: 'save-or-add-step.component.html',
})
export class IntegrationsSaveOrAddStepComponent implements OnInit, OnDestroy {

  integration: Integration;
  flowSubscription: Subscription;

  constructor(
    private currentFlow: CurrentFlow,
    private store: IntegrationStore,
    private route: ActivatedRoute,
    private router: Router,
  ) {
    this.flowSubscription = this.currentFlow.events.subscribe((event: FlowEvent) => {
      this.handleFlowEvent(event);
    });
  }

  cancel() {
    this.router.navigate(['integrations']);
  }

  save() {
    const router = this.router;
    this.currentFlow.events.emit({
      kind: 'integration-save',
      action: (i: Integration) => {
        router.navigate(['/integrations']);
      },
      error: (error) => {
        router.navigate(['/integrations']);
      },
    });
  }

  validateFlow() {
    if (this.currentFlow.getStartConnection() === undefined) {
      this.router.navigate(['connection-select', this.currentFlow.getFirstPosition()], { relativeTo: this.route.parent });
      return;
    }
    if (this.currentFlow.getEndConnection() === undefined) {
      this.router.navigate(['connection-select', this.currentFlow.getLastPosition()], { relativeTo: this.route.parent });
      return;
    }
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-updated':
        this.validateFlow();
        break;
    }
  }

  ngOnInit() {
    this.flowSubscription = this.currentFlow.events.subscribe((event: FlowEvent) => {
      this.handleFlowEvent(event);
    });
    const validate = this.route.queryParams.map(params => params['validate'] || false);
    if (validate) {
      this.validateFlow();
    }
  }

  ngOnDestroy() {
    this.flowSubscription.unsubscribe();
  }
}
