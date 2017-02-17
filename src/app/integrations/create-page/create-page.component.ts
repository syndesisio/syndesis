import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { IntegrationStore } from '../../store/integration/integration.store';
import { Integration } from '../../model';
import { CurrentFlow, FlowEvent } from './current-flow.service';
import { log, getCategory } from '../../logging';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'ipaas-integrations-create-page',
  templateUrl: './create-page.component.html',
  styleUrls: ['./create-page.component.scss'],
})
export class IntegrationsCreatePage implements OnInit, OnDestroy, AfterViewInit {

  integration: Observable<Integration>;
  private readonly loading: Observable<boolean>;

  integrationSubscription: Subscription;
  routeSubscription: Subscription;
  childRouteSubscription: Subscription;
  flowSubscription: Subscription;
  urls: UrlSegment[];
  _canContinue = false;
  position: number;
  pageTitle = 'Create an integration';
  sidebarCollapsed = true;

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

  showNext() {
    if (this.getCurrentChild() === 'connection-select') {
      return false;
    }
    return !(this.getCurrentChild() === 'connection-configure' && this.position === 1);
  }

  showBack() {
    return this.getCurrentChild() === 'connection-configure';
  }

  goBack() {
    const child = this.getCurrentChild();
    switch (child) {
      case 'connection-select':
        // uh...
        break;
      case 'connection-configure':
        // TODO hard-coding this to just go to the previous connection
        this.router.navigate(['connection-select', this.position], { relativeTo: this.route });
        break;
      default:
        // who knows...
        break;
    }

  }

  showFinish() {
    return this.getCurrentChild() === 'connection-configure' && this.position === 1;
  }

  canFinish() {
    return this.currentFlow.isValid();
  }

  canContinue() {
    return this._canContinue;
  }

  finish() {
    const router = this.router;
    this.currentFlow.events.emit({
      kind: 'integration-save',
      action: (i: Integration) => {
        router.navigate(['/integrations']);
      },
      error: (error) => {
        router.navigate(['/integrations']);
      },
    });
  }

  continue() {
    const child = this.getCurrentChild();
    switch (child) {
      case 'connection-select':
        this.router.navigate(['connection-configure', this.position], { relativeTo: this.route });
        break;
      case 'connection-configure':
        // TODO hard-coding this to just go to the next connection
        this.currentFlow.events.emit({
          kind: 'integration-connection-configured',
          position: this.position,
        });
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
      case 'integration-connection-select':
        this.position = event['position'];
        if (this.position === 0 && this.currentFlow.isEmpty()) {
          this.pageTitle = 'Create an integration';
        } else if (this.currentFlow.atEnd(this.position)) {
          this.pageTitle = 'Select end connection';
        } else {
          this.pageTitle = 'Add a connection';
        }

        if (!this.currentFlow.integration.steps[this.position]) {
          this._canContinue = false;
        }
        break;
      case 'integration-selected-connection':
        this.position = event['position'];
        this.currentFlow.events.emit({
          kind: 'integration-set-connection',
          position: this.position,
          connection: event['connection'],
        });
        this._canContinue = true;
        this.continue();
        break;
      case 'integration-connection-configure':
        this.position = event['position'];
        const connection = this.currentFlow.getStep(this.position);
        if (connection) {
          this.pageTitle = 'Configure ' + connection['name'];
        }
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

  ngAfterViewInit() {
    this.sidebarCollapsed = true;
  }

}
