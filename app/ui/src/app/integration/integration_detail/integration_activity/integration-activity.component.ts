import { Component, OnInit, Input } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { PaginationConfig } from 'patternfly-ng';

import { log } from '@syndesis/ui/logging';
import { Integration, IntegrationSupportService, Activity, Step } from '@syndesis/ui/platform';

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

  stepName(step: Step): string {
    if (!step) {
      return 'n/a';
    }
    if (step.name) {
      return step.name;
    }
    if (step.action && step.action.name) {
      return step.action.name;
    }
    return 'n/a';
  }

  fetchActivities(): void {
    this.onRefresh = true;
    this.onError = false;
    this.integrationSupportService
      .requestIntegrationActivity(this.integration.id)
      .do(activitities => {
        // TODO: In a real, efficient RFP environment, this should be performed as a
        //       one step operation within the reducer data logic and never within a component
        activitities.forEach(activity => {
          activity.steps.forEach(step => {
            // XXX: ANTIPATTERN AHEAD. The following code block mutates an object state
            const integrationStep = this.integration.steps.find(_integrationStep => _integrationStep.id == step.id);
            step.name = this.stepName(integrationStep);
            step.isFailed = step.failure && step.failure.length > 0;
            const errorMessages = [null, ...step.messages, step.failure].filter(messages => !!messages);
            step.output = errorMessages.length > 0 ? errorMessages.join('\n') : null;
          });
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
