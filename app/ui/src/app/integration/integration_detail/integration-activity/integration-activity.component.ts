import { Observable } from 'rxjs/Observable';
import { Component, OnInit, Input } from '@angular/core';

import { Integration, IntegrationSupportService, Activity } from '@syndesis/ui/platform';
import { PaginationConfig } from 'patternfly-ng';

@Component({
  selector: 'syndesis-integration-activity',
  templateUrl: './integration-activity.component.html',
  styleUrls: ['./integration-activity.component.scss']
})
export class IntegrationActivityComponent implements OnInit {
  @Input() integration: Integration;
  activities: Activity[] = [];
  onRefresh: boolean;
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

  // TODO: Remove this by a Redux action-driven implementation
  fetchActivities(): void {
    this.onRefresh = true;
    this.integrationSupportService
      .requestIntegrationActivity(this.integration.id)
      .subscribe(activities => {
        this.onRefresh = false;
        this.lastRefresh = new Date();
        this.allActivities = activities;

        this.paginationConfig.totalItems = activities.length;
        this.paginationConfig.pageNumber = 1;
        this.renderActivitiesByPage();
      });
  }

  renderActivitiesByPage(): void {
    const pageItemIndex = (this.paginationConfig.pageNumber - 1) * this.paginationConfig.pageSize;
    this.activities = this.allActivities.slice(pageItemIndex, pageItemIndex + this.paginationConfig.pageSize);
  }
}
