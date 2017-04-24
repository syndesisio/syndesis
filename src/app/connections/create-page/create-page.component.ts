import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { CurrentConnectionService } from './current-connection';
import { Connection, TypeFactory } from '../../model';
import { log, getCategory } from '../../logging';

const category = getCategory('Connections');

@Component({
  selector: 'ipaas-connection-create-page',
  templateUrl: 'create-page.component.html',
  styleUrls: ['./create-page.component.scss'],
})
export class ConnectionsCreatePage implements OnInit, OnDestroy {
  constructor(
    private current: CurrentConnectionService,
    private route: ActivatedRoute,
    private router: Router,
  ) { }

  get connection(): Connection {
    return this.current.connection;
  }

  getCurrentPage() {
    const child = this.route.firstChild;
    if (child && child.snapshot) {
      const path = child.snapshot.url;
      return path[0].path;
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
    this.router.navigate(['..'], { relativeTo: this.route });
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
        if (!this.current.connection.connector.properties || this.current.connection.connector.properties === '') {
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
        if (!this.current.connection.connector.properties || this.current.connection.connector.properties === '') {
          target.push('review');
        } else {
          target.push('configure-fields');
        }
        break;
      case 'configure-fields':
        target.push('review');
        break;
      case 'review':
        this.current.events.emit({
          kind: 'connection-save-connection',
          connection: this.current.connection,
          action: (connection: Connection) => {
            this.router.navigate(['..'], { relativeTo: this.route });
          },
          error: (reason: any) => {
            log.debugc(() => 'Error creating connection: ' + JSON.stringify(reason, undefined, 2), category);
          },
        });
        // TODO
        break;
      default:
        break;
    }
    if (target.length) {
      this.router.navigate(target, { relativeTo: this.route });
    }
  }

  ngOnInit() {
    this.current.connection = TypeFactory.createConnection();
    // we always want to start at the beginning of the wizard on a refresh
    if (this.getCurrentPage() !== 'connection-basics') {
      this.router.navigate(['connection-basics'], { relativeTo: this.route });
    }
    $.fn.setupVerticalNavigation ? $.fn.setupVerticalNavigation().hideMenu() : '';
  }

  ngOnDestroy() {
    $.fn.setupVerticalNavigation ? $.fn.setupVerticalNavigation().showMenu() : '';
  }
}
