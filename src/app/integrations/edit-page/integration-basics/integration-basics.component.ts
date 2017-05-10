import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { FlowPage } from '../flow-page';

@Component({
  selector: 'syndesis-integrations-integration-basics',
  templateUrl: 'integration-basics.component.html',
  styleUrls: ['./integration-basics.component.scss'],
})
export class IntegrationBasicsComponent extends FlowPage {

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public detector: ChangeDetectorRef,
    ) {
    super(currentFlow, route, router, detector);
  }

  canContinue() {
    return this.currentFlow.integration.name && this.currentFlow.integration.name !== '';
  }

  continue() {
    this.router.navigate(['save-or-add-step'], { queryParams: { validate: true }, relativeTo: this.route.parent });
  }

  get name(): string {
    const name = this.currentFlow.integration.name || '';
    return name;
  }

  set name(name: string) {
    this.currentFlow.events.emit({
      kind: 'integration-set-property',
      property: 'name',
      value: name,
    });
  }

  get description(): string {
    return this.currentFlow.integration.description || '';
  }

  set description(description: string) {
    this.currentFlow.events.emit({
      kind: 'integration-set-property',
      property: 'description',
      value: description,
    });
  }

  get tagsArray(): string[] {
    return this.currentFlow.integration.tags;
  }

  get tags(): string {
    return this.tagsArray.join(', ');
  }

  set tags(tags: string) {
    const _tags = tags.split(',').map((str) => str.trim());
    this.currentFlow.events.emit({
      kind: 'integration-set-property',
      property: 'tags',
      value: _tags,
    });
  }

}
