import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { CurrentFlow } from '../current-flow.service';
import { FlowPage } from '../flow-page';
import { TourService } from 'ngx-tour-ngx-bootstrap';
import { UserService } from '../../../common/user.service';

@Component({
  selector: 'syndesis-integrations-integration-basics',
  templateUrl: 'integration-basics.component.html',
<<<<<<< HEAD
  styleUrls: ['./integration-basics.component.scss']
})
export class IntegrationBasicsComponent extends FlowPage {
  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public detector: ChangeDetectorRef
  ) {
=======
  styleUrls: [ './integration-basics.component.scss' ],
})
export class IntegrationBasicsComponent extends FlowPage implements OnInit {
  constructor(public currentFlow: CurrentFlow,
              public route: ActivatedRoute,
              public router: Router,
              public detector: ChangeDetectorRef,
              public tourService: TourService,
              private userService: UserService) {
>>>>>>> feat(guided-tour): Add remaining steps to guided tour
    super(currentFlow, route, router, detector);
  }

  canContinue() {
    return (
      this.currentFlow.integration.name &&
      this.currentFlow.integration.name !== ''
    );
  }

  continue() {
    this.router.navigate([ 'save-or-add-step' ], {
      queryParams: { validate: true },
      relativeTo: this.route.parent
    });
  }

  get name(): string {
    const name = this.currentFlow.integration.name || '';
    return name;
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
    if (this.userService.getTourState() === true) {
      this.tourService.initialize([ {
          anchorId: 'integrations.publish',
          title: 'Publish',
          content: 'Click Publish to start running the integration, which will take a moment or two. ' +
          'Click Save as Draft to save the integration without deploying it.',
          placement: 'left',
        } ],
      );
      setTimeout(() => this.tourService.start());
    }
  }
}
