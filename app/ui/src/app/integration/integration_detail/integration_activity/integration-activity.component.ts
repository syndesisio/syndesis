import { Component, OnInit, Input } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { forkJoin } from 'rxjs/observable/forkJoin';
import { PaginationConfig } from 'patternfly-ng';

import { log } from '@syndesis/ui/logging';
import { Integration, IntegrationSupportService, Activity, Step, IntegrationDeployment } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-integration-activity',
  templateUrl: './integration-activity.component.html',
  styleUrls: ['./integration-activity.component.scss']
})
export class IntegrationActivityComponent implements OnInit {
  @Input() integration: Integration;
  activities: Activity[] = [];
  onRefresh: boolean;
  onError: boolean;
  showPagination: boolean;
  lastRefresh = new Date();
  paginationConfig: PaginationConfig = {
    pageSize: 15,
    pageSizeIncrements: [15, 40, 75]
  };

  selectedActivity: Activity;

  private allActivities: Activity[] = [];

  constructor(private integrationSupportService: IntegrationSupportService) { }

  ngOnInit() {
    this.fetchActivities();
  }

  fetchStepName(step: Step): string {
    let stepName = 'n/a';

    if (step) {
      const { name, action } = step;
      stepName = name || action && action.name ? action.name : stepName;
    }

    return stepName;
  }

  fetchActivities(): void {
    this.onRefresh = true;
    this.onError = false;

    const activities$ = this.integrationSupportService.requestIntegrationActivity(this.integration.id);
    const integrationDeployments$ = this.integrationSupportService.getDeployments(this.integration.id);

    forkJoin<[Activity[], IntegrationDeployment[]]>([activities$, integrationDeployments$]).map(results => {
      const activitities = results[0];
      const integrationDeployments = results[1];

      activitities.forEach(activity => {
        if (activity.steps && Array.isArray(activity.steps)) {
          activity.steps.forEach(step => {
            const deployedIntegrationStep = integrationDeployments
              .find(deployment => deployment.version === +activity.ver)
              .spec
              .steps.find(integrationStep => integrationStep.id == step.id);

            step.name = this.fetchStepName(deployedIntegrationStep);
            step.isFailed = step.failure && step.failure.length > 0;

            const errorMessages = [null, ...step.messages, step.failure].filter(messages => !!messages);
            step.output = errorMessages.length > 0 ? errorMessages.join('\n') : null;
          });
        }
      });

      return activitities;
    })
    .subscribe(
      activities => this.updateActivities(activities),
      error => this.handleError(error)
    );
  }

  renderActivitiesByPage(): void {
    const pageItemIndex = (this.paginationConfig.pageNumber - 1) * this.paginationConfig.pageSize;
    this.activities = this.allActivities.slice(pageItemIndex, pageItemIndex + this.paginationConfig.pageSize);
  }

  onSelectActivity(event: Event, activity: Activity): void {
    event.preventDefault();
    event.stopPropagation();
    this.selectedActivity = this.selectedActivity && this.selectedActivity.id === activity.id ? null : activity;
  }

  private updateActivities(activities: Activity[]): void {
    this.onRefresh = false;
    this.lastRefresh = new Date();

    const aggregatedActivities = [...activities, ...this.allActivities];
    this.allActivities = Array.from(new Set(aggregatedActivities.map(activity => activity.id)))
      .map(id => aggregatedActivities.find(activity => activity.id === id))
      .sort((activity1, activity2) => activity2.at - activity1.at);

    this.showPagination = (this.allActivities.length > this.paginationConfig.pageSize);
    this.paginationConfig.totalItems = this.allActivities.length;
    this.paginationConfig.pageNumber = 1;
    this.renderActivitiesByPage();
  }

  private handleError(error: Error): void {
    this.onRefresh = false;
    this.onError = true;
    log.error(`Error fetching activity records for integration ID ${this.integration.id}`, error);
  }
}
