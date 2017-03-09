import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { IntegrationStore } from '../../store/integration/integration.store';
import { Integration } from '../../model';
import { CurrentFlow, FlowEvent } from './current-flow.service';
import { log, getCategory } from '../../logging';

const category = getCategory('IntegrationsEditPage');

@Component({
  selector: 'ipaas-integrations-edit-page',
  templateUrl: './edit-page.component.html',
  styleUrls: [ './edit-page.component.scss' ],
})
export class IntegrationsEditPage implements OnInit, OnDestroy {

  integration: Observable<Integration>;
  private readonly loading: Observable<boolean>;

  integrationSubscription: Subscription;
  routeSubscription: Subscription;
  childRouteSubscription: Subscription;
  flowSubscription: Subscription;
  urls: UrlSegment[];
  _canContinue = false;
  position: number;

  constructor( private currentFlow: CurrentFlow,
              private store: IntegrationStore,
              private route: ActivatedRoute,
              private router: Router,
              private detector: ChangeDetectorRef,
               ) {
    this.integration = this.store.resource;
    this.loading = this.store.loading;
  }

  getCurrentChild(): string {
    const child = this.route.firstChild;
    if (child && child.snapshot) {
      const path = child.snapshot.url;
      // log.debugc(() => 'path from root: ' + path, category);
      return path[ 0 ].path;
    } else {
      // log.debugc(() => 'no current child', category);
      return undefined;
    }
  }

  getCurrentPosition(): number {
    const child = this.route.firstChild;
    if (child && child.snapshot) {
      const path = child.snapshot.url;
      // log.debugc(() => 'path from root: ' + path, category);
      try {
        const position = path[1].path;
        return +position;
      } catch (error) {
        return -1;
      }
    } else {
      // log.debugc(() => 'no current child', category);
      return undefined;
    }
  }

  handleFlowEvent(event: FlowEvent) {
    const child = this.getCurrentChild();
    // TODO we could probably tidy up the unused cases at some point
    switch (event.kind) {
      case 'integration-updated':
        this.router.navigate(['save-or-add-step'], { relativeTo: this.route });
        this.detector.detectChanges();
        break;
      case 'integration-no-actions':
        break;
      case 'integration-no-connections':
        break;
      case 'integration-action-select':
      case 'integration-connection-select':
        break;
      case 'integration-selected-action':
        break;
      case 'integration-selected-connection':
        break;
      case 'integration-action-configure':
      case 'integration-connection-configure':
        break;
    }
  }

  ngOnInit() {
    this.flowSubscription = this.currentFlow.events.subscribe((event: FlowEvent) => {
      this.handleFlowEvent(event);
    });
    this.integrationSubscription = this.integration.subscribe((i: Integration) => {
      this.currentFlow.integration = i;
    });
    this.routeSubscription = this.route.params.pluck<Params, string>('integrationId')
      .map((integrationId: string) => this.store.loadOrCreate(integrationId))
      .subscribe();
    $.fn.setupVerticalNavigation ? $.fn.setupVerticalNavigation().hideMenu() : '';
  }

  ngOnDestroy() {
    this.integrationSubscription.unsubscribe();
    this.routeSubscription.unsubscribe();
    this.flowSubscription.unsubscribe();
    $.fn.setupVerticalNavigation ? $.fn.setupVerticalNavigation().showMenu() : '';
  }

}
