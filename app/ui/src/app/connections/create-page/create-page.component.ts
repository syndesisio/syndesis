import {
  Component,
  OnInit,
  OnDestroy,
  ViewChild,
  TemplateRef
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { Connection } from '@syndesis/ui/platform';
import { TypeFactory } from '@syndesis/ui/model';
import { NavigationService, ModalService } from '@syndesis/ui/common';
import {
  CurrentConnectionService,
  ConnectionEvent
} from '@syndesis/ui/connections/create-page/current-connection';

const CONNECTION_BASICS = 'connection-basics';
const CONFIGURE_FIELDS = 'configure-fields';
const REVIEW = 'review';

const CONNECTION_BASICS_INDEX = 1;
const CONFIGURE_FIELDS_INDEX = 2;
const REVIEW_INDEX = 3;

@Component({
  selector: 'syndesis-connection-create-page',
  templateUrl: 'create-page.component.html',
  styleUrls: ['./create-page.component.scss']
})
export class ConnectionsCreatePage implements OnInit, OnDestroy {
  _currentActiveStep = CONNECTION_BASICS_INDEX;
  currentSubscription: Subscription;
  fragmentSubscription: Subscription;

  @ViewChild('cancelModalTemplate') cancelModalTemplate: TemplateRef<any>;

  private cancelModalId = 'create-cancellation-modal';

  constructor(
    public current: CurrentConnectionService,
    private route: ActivatedRoute,
    private router: Router,
    private nav: NavigationService,
    private modalService: ModalService,
  ) {}

  get connection(): Connection {
    return this.current.connection;
  }

  get currentActiveStep() {
    const page = this.getCurrentPage();
    switch (page) {
      case CONNECTION_BASICS:
        return CONNECTION_BASICS_INDEX;
      case CONFIGURE_FIELDS:
        return CONFIGURE_FIELDS_INDEX;
      case REVIEW:
        return REVIEW_INDEX;
      default:
        return 0;
    }
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
    switch (page) {
      case CONNECTION_BASICS:
        return this.connection.name && this.connection.connector;
      case CONFIGURE_FIELDS:
        return this.current.formGroup && this.current.formGroup.valid;
      case REVIEW:
        return this.current.formGroup && this.current.formGroup.valid;
      default:
        return true;
    }
  }

  showBack() {
    const page = this.getCurrentPage();
    switch (page) {
      case CONNECTION_BASICS:
        return false;
      default:
        return true;
    }
  }

  showNextButton() {
    const page = this.getCurrentPage();
    switch (page) {
      case CONNECTION_BASICS:
        return false;
      case CONFIGURE_FIELDS:
        return !this.current.hasCredentials();
      case REVIEW:
        return false;
      default:
        return true;
    }
  }

  showCancelModal() {
    this.modalService.show(this.cancelModalId).then(modal => {
      if (modal.result) {
        this.cancel();
      }
    });
  }

  onCancel(doCancel: boolean): void {
    this.modalService.hide(this.cancelModalId, doCancel);
  }

  cancel() {
    this.router.navigate(['cancel'], { relativeTo: this.route });
  }

  goBack() {
    const page = this.getCurrentPage();
    const target = [];
    switch (page) {
      case CONNECTION_BASICS:
        target.push('..');
        break;
      case CONFIGURE_FIELDS:
        target.push(CONNECTION_BASICS);
        break;
      case REVIEW:
        if (!this.current.connection.connector.properties) {
          target.push(CONNECTION_BASICS);
        } else {
          target.push(CONFIGURE_FIELDS);
        }
        break;
      default:
        break;
    }
    if (target.length) {
      this.router.navigate(target, { relativeTo: this.route });
    }
  }

  /**
   *  TODO this is terrible, the page flow should be handled in the individual steps
   */
  goForward() {
    const page = this.getCurrentPage();
    const target = [];
    switch (page) {
      case CONNECTION_BASICS:
        if (!this.current.hasProperties()) {
          target.push(REVIEW);
        } else {
          target.push(CONFIGURE_FIELDS);
        }
        break;
      case CONFIGURE_FIELDS:
        target.push(REVIEW);
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
      kind: 'connection-trigger-create'
    });
  }

  handleEvent(event: ConnectionEvent) {
    const page = this.getCurrentPage();
    switch (event.kind) {
      case 'connection-set-connection':
        if (!this.current.hasConnector() && page !== CONNECTION_BASICS) {
          setTimeout(() => {
            this.router.navigate([CONNECTION_BASICS], {
              relativeTo: this.route
            });
          }, 10);
          return;
        } else if (
          this.current.hasConnector() &&
          page === CONNECTION_BASICS
        ) {
          this.goForward();
        }
        if (
          this.current.oauthStatus &&
          this.current.oauthStatus.status === 'SUCCESS' &&
          page === REVIEW
        ) {
          this.goForward();
          return;
        }
        break;
      default:
        break;
    }
  }

  ngOnInit() {
    this.current.init();
    this.currentSubscription = this.current.events.subscribe(event => {
      this.handleEvent(event);
    });
    this.fragmentSubscription = this.route.fragment.subscribe(fragment => {
      /**
       * Detect if there's a selected connection ID already or not
       */
      if (this.current.connection && this.current.connection.connectorId) {
        return;
      }
      const connection = TypeFactory.create<Connection>();
      if (fragment) {
        const status = JSON.parse(decodeURIComponent(fragment));
        this.current.oauthStatus = status;
        connection.connectorId = status.connectorId;
      }
      this.current.connection = connection;
    });
    this.nav.hide();
    this.modalService.registerModal(
      this.cancelModalId,
      this.cancelModalTemplate
    );
  }

  ngOnDestroy() {
    this.currentSubscription.unsubscribe();
    this.fragmentSubscription.unsubscribe();
    this.current.dispose();
    this.nav.show();
    this.modalService.unregisterModal(this.cancelModalId);
  }
}
