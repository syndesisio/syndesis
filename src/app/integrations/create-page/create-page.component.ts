import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { IntegrationStore } from '../../store/integration/integration.store';
import { Integration } from '../../store/integration/integration.model';
import { CurrentFlow, FlowEvent } from './current-flow.service';
import { log, getCategory } from '../../logging';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'ipaas-integrations-create-page',
  templateUrl: './create-page.component.html',
  styleUrls: ['./create-page.component.scss'],
})
export class IntegrationsCreatePage implements OnInit, OnDestroy {

  integration: Observable<Integration>;
  private readonly loading: Observable<boolean>;

  integrationSubscription: Subscription;
  routeSubscription: Subscription;
  childRouteSubscription: Subscription;
  flowSubscription: Subscription;
  urls: UrlSegment[];
  canContinue = false;
  position: number;

  constructor(
    private currentFlow: CurrentFlow,
    private store: IntegrationStore,
    private route: ActivatedRoute,
    private router: Router,
    ) {
    this.integration = this.store.resource;
    this.loading = this.store.loading;
  }

  cancel() {
    this.router.navigate(['/integrations']);
  }

  finish() {
    this.router.navigate(['/integrations']);
  }

  continue() {
    const child = this.getCurrentChild();
    switch (child) {
      case 'connection-select':
        this.router.navigate(['connection-configure', this.position], { relativeTo: this.route });
        break;
      case 'connection-configure':
        // TODO hard-coding this to just go to the next connection
        this.router.navigate(['connection-select', this.position + 1], { relativeTo: this.route });
        break;
      default:
        // who knows...
        break;
    }
  }

  getCurrentChild(): string {
    const child = this.route.firstChild;
    if (child && child.snapshot) {
      const path = child.snapshot.url;
      // log.debugc(() => 'path from root: ' + path, category);
      return path[0].path;
    } else {
      // log.debugc(() => 'no current child', category);
      return undefined;
    }
  }

  handleFlowEvent(event: FlowEvent) {
    const child = this.getCurrentChild();
    switch (event.kind) {
      case 'integration-no-connections':
        if (child !== 'connection-select') {
          this.router.navigate(['connection-select', 0], { relativeTo: this.route });
        }
        break;
      case 'integration-selected-connection':
        this.position = event['position'];
        this.currentFlow.events.emit({
          kind: 'integration-set-connection',
          position: this.position,
          connection: event['connection'],
        });
        this.canContinue = true;
        break;
    }
  }

  ngOnInit() {
    this.flowSubscription = this.currentFlow.events.subscribe((event: FlowEvent) => {
      this.handleFlowEvent(event);
    });
    this.routeSubscription = this.route.params.pluck<Params, string>('integrationId')
      .map((integrationId: string) => this.store.loadOrCreate(integrationId))
      .subscribe();
    this.integrationSubscription = this.integration.subscribe((i: Integration) => {
      this.currentFlow.integration = i;
    });
  }

  ngOnDestroy() {
    this.integrationSubscription.unsubscribe();
    this.routeSubscription.unsubscribe();
    this.flowSubscription.unsubscribe();
  }

}
