import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { IntegrationStore } from '../../../store/integration/integration.store';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { FlowPage } from '../flow-page';
import { Integration, Step, TypeFactory } from '../../../model';

@Component({
  selector: 'ipaas-integrations-save-or-add-step',
  templateUrl: 'save-or-add-step.component.html',
  styleUrls: ['./save-or-add-step.component.scss'],
})
export class IntegrationsSaveOrAddStepComponent extends FlowPage implements OnInit, OnDestroy {

  integration: Integration;

  constructor(
    public currentFlow: CurrentFlow,
    public store: IntegrationStore,
    public route: ActivatedRoute,
    public router: Router,
  ) {
    super(currentFlow, route, router);
  }

  goBack() { /* this should be a no-op */ }

  save() {
    this.currentFlow.integration.statusType = 'Deactivated';
    this.doSave();
  }

  saveAndPublish() {
    this.currentFlow.integration.statusType = 'Activated';
    this.doSave();
  }

  doSave() {
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
    if (!this.currentFlow.integration.name || this.currentFlow.integration.name === '' ||
        !this.currentFlow.integration.description || this.currentFlow.integration.description === '') {
      this.router.navigate(['integration-basics'], { relativeTo: this.route.parent });
      return;
    }
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
    const validate = this.route.queryParams.map(params => params['validate'] || false);
    if (validate) {
      this.validateFlow();
    }
  }

}
