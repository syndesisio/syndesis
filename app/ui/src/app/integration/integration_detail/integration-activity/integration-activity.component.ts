import { Component, OnInit, Input } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { PaginationConfig } from 'patternfly-ng';

import { log } from '@syndesis/ui/logging';
import { Integration, IntegrationSupportService, Activity } from '@syndesis/ui/platform';

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

  private allActivities: Activity[] = [];

  constructor(private integrationSupportService: IntegrationSupportService) { }

  ngOnInit() {
    this.fetchActivities();
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
            step.name = integrationStep ? integrationStep.name : 'n/a';
            step.isFailed = (step.failure && step.failure.length > 0) || (step.message && step.message.length > 0);
            const errorMessages = [step.failure, ...step.message].filter(messages => !!messages);
            step.output = errorMessages.length > 0 ? errorMessages.join('. ') : 'N/A';
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

  private updateActivities(activities: Activity[]): void {
    this.onRefresh = false;
    this.lastRefresh = new Date();
    this.showPagination = (activities.length > this.paginationConfig.pageSize);
    this.allActivities = activities.sort((activity1, activity2) => {
      return activity1.at < activity1.at ? -1 : activity1.at > activity1.at ? 1 : 0;
    });

    this.paginationConfig.totalItems = activities.length;
    this.paginationConfig.pageNumber = 1;
    this.renderActivitiesByPage();
  }

  private handleError(error: Error): void {
    this.onRefresh = false;
    this.onError = true;
    log.error(`Error fetching activity records for integration ID ${this.integration.id}`, error);
  }
}
