import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CurrentFlow, FlowPage } from '@syndesis/ui/integration/edit-page';

@Component({
  selector: 'syndesis-integration-integration-basics',
  templateUrl: 'integration-basics.component.html',
  styleUrls: ['./integration-basics.component.scss']
})
export class IntegrationBasicsComponent extends FlowPage {
  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router
  ) {
    super(currentFlow, route, router);
  }

  canContinue() {
    const integrationName = this.currentFlow.integration.name;
    return integrationName && integrationName !== '';
  }

  continue() {
    this.router.navigate(['save-or-add-step'], {
      queryParams: { validate: true },
      relativeTo: this.route.parent
    });
  }

  get name(): string {
    return this.currentFlow.integration.name || '';
  }

  set name(name: string) {
    this.currentFlow.events.emit({
      kind: 'integration-set-property',
      property: 'name',
      value: name
    });
  }

  get description(): string {
    return this.currentFlow.integration.description || '';
  }

  set description(description: string) {
    this.currentFlow.events.emit({
      kind: 'integration-set-property',
      property: 'description',
      value: description
    });
  }

  get tagsArray(): string[] {
    return this.currentFlow.integration.tags;
  }

  get tags(): string {
    return this.tagsArray.join(', ');
  }

  set tags(tags: string) {
    const _tags = tags.split(',').map(str => str.trim());
    this.currentFlow.events.emit({
      kind: 'integration-set-property',
      property: 'tags',
      value: _tags
    });
  }
}
