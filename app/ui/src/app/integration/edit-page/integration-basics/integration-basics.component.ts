import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  CurrentFlowService,
  FlowPageService,
  INTEGRATION_SET_PROPERTY,
} from '@syndesis/ui/integration/edit-page';

@Component({
  selector: 'syndesis-integration-integration-basics',
  templateUrl: 'integration-basics.component.html',
  styleUrls: [
    '../../integration-common.scss',
    './integration-basics.component.scss',
  ],
})
export class IntegrationBasicsComponent implements OnInit, OnDestroy {
  private targetUrl: string;
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
    if (this.targetUrl != null) {
      this.router.navigateByUrl(this.targetUrl);
    } else {
      this.router.navigate(['save-or-add-step'], {
        queryParams: { validate: true },
        relativeTo: this.route.parent,
      });
    }
  }

  get name(): string {
    return this.currentFlowService.integration.name || '';
  }

  set name(name: string) {
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SET_PROPERTY,
      property: 'name',
      value: name,
    });
  }

  get description(): string {
    return this.currentFlowService.integration.description || '';
  }

  set description(description: string) {
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SET_PROPERTY,
      property: 'description',
      value: description,
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
      kind: INTEGRATION_SET_PROPERTY,
      property: 'tags',
      value: _tags,
    });
  }

  ngOnInit() {
    this.flowPageService.initialize();
    this.flowPageService.showCancel = false;

    this.route.queryParamMap.subscribe(params => this.targetUrl = params.get('targetUrl'));
  }

  ngOnDestroy() {
    this.flowPageService.showCancel = true;
  }
}
