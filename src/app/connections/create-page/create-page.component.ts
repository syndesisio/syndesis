import { AfterViewInit, Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { NavigationService } from '../../common/navigation.service';
import {
  CurrentConnectionService,
  ConnectionEvent,
} from './current-connection';
import { Connection, TypeFactory } from '../../model';
import { log, getCategory } from '../../logging';
import { CanComponentDeactivate } from '../../common/can-deactivate-guard.service';
import { TourService } from 'ngx-tour-ngx-bootstrap';

const category = getCategory('Connections');

@Component({
  selector: 'syndesis-connection-create-page',
  templateUrl: 'create-page.component.html',
  styleUrls: [ './create-page.component.scss' ],
})
export class ConnectionsCreatePage implements OnInit, OnDestroy, AfterViewInit {
  private routerEventsSubscription: Subscription;

  constructor(private current: CurrentConnectionService,
              private route: ActivatedRoute,
              private router: Router,
              private nav: NavigationService,
              private detector: ChangeDetectorRef,
              public tourService: TourService,
              ) {
  }

  get connection(): Connection {
    return this.current.connection;
  }

  getCurrentPage() {
    const child = this.route.firstChild;
    if (child && child.snapshot) {
      const path = child.snapshot.url;
      return path[ 0 ].path;
    } else {
      return undefined;
    }
  }

  canContinue() {
    const page = this.getCurrentPage();
    const connection = this.current.connection;
    switch (page) {
      case 'connection-basics':
        return this.connection.name && this.connection.connector;
      case 'configure-fields':
      // TODO validate form
      case 'review':
      // TODO is this ever going to be false?
    }
    return true;
  }

  showBack() {
    const page = this.getCurrentPage();
    switch (page) {
      case 'connection-basics':
        return false;
    }
    return true;
  }

  showNext() {
    const page = this.getCurrentPage();
    switch (page) {
      case 'connection-basics':
        return false;
    }
    return true;
  }

  cancel() {
    this.router.navigate([ 'cancel' ], { relativeTo: this.route });
  }

  goBack() {
    const page = this.getCurrentPage();
    const target = [];
    switch (page) {
      case 'connection-basics':
        target.push('..');
        break;
      case 'configure-fields':
        target.push('connection-basics');
        break;
      case 'review':
        if (
          !this.current.connection.connector.properties ||
          this.current.connection.connector.properties === ''
        ) {
          target.push('connection-basics');
        } else {
          target.push('configure-fields');
        }
        break;
      default:
        break;
    }
    if (target.length) {
      this.router.navigate(target, { relativeTo: this.route });
    }
  }

  // TODO this is terrible, the page flow should be handled in the individual steps
  goForward() {
    const page = this.getCurrentPage();
    const target = [];
    switch (page) {
      case 'connection-basics':
        if (
          !this.current.connection.connector.properties ||
          this.current.connection.connector.properties === ''
        ) {
          target.push('review');
        } else {
          target.push('configure-fields');
        }
        break;
      case 'configure-fields':
        target.push('review');
        break;
      default:
        break;
    }
    if (target.length) {
      this.router.navigate(target, { relativeTo: this.route });
    }
  }

  doCreate() {
    this.current.events.emit({
      kind: 'connection-trigger-create',
    });
  }

  handleEvent(event: ConnectionEvent) {
    const page = this.getCurrentPage();
    switch (event.kind) {
      case 'connection-set-connection':
        log.infoc(
          () => 'Credentials: ' + JSON.stringify(this.current.credentials),
        );
        log.infoc(() => 'hasCredentials: ' + this.current.hasCredentials());
        if (!this.current.hasConnector() && page !== 'connection-basics') {
          setTimeout(() => {
            this.router.navigate([ 'connection-basics' ], {
              relativeTo: this.route,
            });
          }, 10);
          return;
        } else if (this.current.hasConnector() && page === 'connection-basics') {
          this.goForward();
        }
        if (
          this.current.oauthStatus &&
          this.current.oauthStatus.status === 'SUCCESS' &&
          page === 'configure-fields'
        ) {
          this.goForward();
          return;
        }
        break;
    }
    try {
      this.detector.detectChanges();
    } catch (err) {
    }
  }

  ngOnInit() {
    this.current.init();
    this.current.events.subscribe(event => {
      this.handleEvent(event);
    });
    this.route.fragment.subscribe(fragment => {
      // detect if there's a selected connection ID already or not
      if (this.current.connection && this.current.connection.connectorId) {
        return;
      }
      const connection = TypeFactory.createConnection();
      if (fragment) {
        const status = JSON.parse(decodeURIComponent(fragment));
        this.current.oauthStatus = status;
        connection.connectorId = status.connectorId;
      }
      this.current.connection = connection;
    });
    this.nav.hide();
    this.routerEventsSubscription = this.router.events.subscribe(event => {
      this.detector.detectChanges();
    });
  }

  ngAfterViewInit() {
    /*
    this.tourService.initialize([ {
        route: 'connections/create/connection-basics',
        anchorId: 'connections.type',
        content: 'A connection represents a specific application that you want to obtain data from or send data to.',
        placement: 'left',
        title: 'Connection',
      } ],
      {
        route: '',
      },
    );*/

    //this.tourService.start();
  }

  ngOnDestroy() {
    this.current.dispose();
    this.nav.show();
    this.routerEventsSubscription.unsubscribe();
  }
}
