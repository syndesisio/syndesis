import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  CurrentFlowService,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';

@Component({
  selector: 'syndesis-integration-integration-basics',
  templateUrl: 'integration-basics.component.html',
  styleUrls: [
    '../../integration-common.scss',
    './integration-basics.component.scss'
  ]
})
export class IntegrationBasicsComponent implements OnInit {
  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router
  ) {}

  get errorMessage() {
    return this.flowPageService.errorMessage;
  }

  get saveInProgress() {
    return this.flowPageService.saveInProgress;
  }

  get publishInProgress() {
    return this.flowPageService.publishInProgress;
  }

  cancel() {
    this.flowPageService.cancel();
  }

  save() {
    this.flowPageService.save(this.route);
  }

  publish() {
    this.flowPageService.publish(this.route);
  }

  canContinue() {
    const integrationName = this.currentFlowService.integration.name;
    return integrationName && integrationName !== '';
  }

  continue() {
    this.router.navigate(['save-or-add-step'], {
      queryParams: { validate: true },
      relativeTo: this.route.parent
    });
  }

  get name(): string {
    return this.currentFlowService.integration.name || '';
  }

  set name(name: string) {
    this.currentFlowService.events.emit({
      kind: 'integration-set-property',
      property: 'name',
      value: name
    });
  }

  get description(): string {
    return this.currentFlowService.integration.description || '';
  }

  set description(description: string) {
    this.currentFlowService.events.emit({
      kind: 'integration-set-property',
      property: 'description',
      value: description
    });
  }

  get tagsArray(): string[] {
    return this.currentFlowService.integration.tags;
  }

  get tags(): string {
    return this.tagsArray.join(', ');
  }

  set tags(tags: string) {
    const _tags = tags.split(',').map(str => str.trim());
    this.currentFlowService.events.emit({
      kind: 'integration-set-property',
      property: 'tags',
      value: _tags
    });
  }

  ngOnInit() {
    this.flowPageService.initialize();
  }
}
