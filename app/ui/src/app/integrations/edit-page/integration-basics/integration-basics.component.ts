import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { CurrentFlow, FlowPage } from '@syndesis/ui/integrations/edit-page';
import { TourService } from 'ngx-tour-ngx-bootstrap';
import { UserService } from '@syndesis/ui/common';

@Component({
  selector: 'syndesis-integrations-integration-basics',
  templateUrl: 'integration-basics.component.html',
  styleUrls: ['./integration-basics.component.scss']
})
export class IntegrationBasicsComponent extends FlowPage implements OnInit {
  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public tourService: TourService,
    private userService: UserService
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

  ngOnInit() {
    /**
     * If guided tour state is set to be shown (i.e. true), then show it for this page, otherwise don't.
     */
    /*
    if (this.userService.getTourState() === true) {
      this.tourService.initialize([{
          anchorId: 'integrations.publish',
          title: 'Publish',
          content: `Click Publish to start running the integration, which will
            take a moment or two.  Click Save as Draft to save the integration
            without deploying it.`,
          placement: 'left',
        }],
      );
      setTimeout(() => this.tourService.start());
    }
    */
  }
}
