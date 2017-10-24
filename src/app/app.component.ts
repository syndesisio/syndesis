import { AfterViewInit, ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Response } from '@angular/http';
import { saveAs } from 'file-saver';
import { Restangular } from 'ngx-restangular';
import {
  Notification,
  NotificationEvent,
  NotificationService,
  NotificationType,
} from 'patternfly-ng';
import { TourService } from 'ngx-tour-ngx-bootstrap';
import { Observable } from 'rxjs/Observable';
import { ModalService } from './common/modal/modal.service';
import { NavigationService } from './common/navigation.service';
import { UserService } from './common/user.service';
import { ConfigService } from './config.service';
import { log } from './logging';
import { User } from './model';
import { TestSupportService } from './store/test-support.service';

@Component({
  selector: 'syndesis-root',
  templateUrl: './app.component.html',
  styleUrls: [ './app.component.scss' ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [ Restangular, TestSupportService ],
})
export class AppComponent implements OnInit, AfterViewInit {
  // TODO icon?
  /**
   * Logo with white background.
   */
  logoWhiteBg = 'assets/images/syndesis-logo-svg-white.svg';
  iconWhiteBg = 'assets/images/glasses_logo_square.png';

  /**
   * Logo with dark background
   */
  logoDarkBg = 'assets/images/syndesis-logo-svg-white.svg';
  iconDarkBg = 'assets/images/glasses_logo_square.png';

  /**
   * @type {boolean}
   * Flag used to determine whether or not the user is logged in.
   */
  loggedIn = true;

  /**
   * @type {boolean}
   * Flag used to determine whether or not the user is a first time user.
   */
  firstTime = false;

  productBuild = false;

  /**
   * @type {string}
   * Title of application. Used in the browser title tag.
   */
  title = 'Syndesis';
  user: Observable<User>;

  notifications: Notification[];

  /**
   * Local var used to determine whether or not to display a close
   * button on a PatternFly toast notification.
   */
  showClose: boolean;

  constructor(private config: ConfigService,
              private userService: UserService,
              public testSupport: TestSupportService,
              private notificationService: NotificationService,
              private nav: NavigationService,
              private modalService: ModalService,
              public tourService: TourService) {
    this.tourService.initialize([ {
        route: 'settings',
        title: 'Get Started',
        content: 'This series of popups acquaints you with Fuse Ignite. When you are ready to create a sample integration, ' +
        'click the help icon and select Documentation to get step-by-step instructions.',
        anchorId: 'get.started',
        placement: 'bottom',
      }, {
        route: 'dashboard',
        title: 'Create Integration',
        content: 'After creating at least two connections, you can create an integration.',
        anchorId: 'dashboard.integration',
        placement: 'bottom',
      }, {
        route: 'connections/create/connection-basics',
        title: 'Connection',
        content: 'A connection represents a specific application that you want to obtain data from or send data to.',
        anchorId: 'angular-ui-tour',
        placement: 'left',
      }, {
        route: 'connections/create/review',
        title: 'Make It Available',
        content: 'Click Create to make the new connection available for use in integrations.',
        anchorId: 'usage',
        placement: 'bottom',
      }, {
        route: 'integrations/create/connection-select/0',
        title: 'Available Connections',
        content: 'After at least two connections are available, you can create an integration that uses the connections you choose.',
        anchorId: 'integrations.availableCon',
        placement: 'top',
      }, {
        route: 'integrations/create/connection-select/0',
        title: 'Integration Panel',
        content: 'As you create an integration, see its connections and steps in the order ' +
        'in which they occur when the integration is running.',
        anchorId: 'integrations.panel',
        placement: 'right',
      }, {
        route: 'integrations/create/action-select/0',
        title: 'Available Actions',
        content: 'When an integration uses the selected connection it performs the action you select.',
        anchorId: 'integrations.availableAct',
        placement: 'top',
      }, {
        route: 'integrations/create/action-configure/0/0',
        title: 'Done',
        content: 'Clicking Done adds the finish connection to the integration. ' +
        'You can then add one or more steps that operate on the data.',
        anchorId: 'integrations.done',
        placement: 'bottom',
      }, {
        route: 'integrations/create/save-or-add-step?validate=true',
        title: 'Operate On Data',
        content: 'Clicking the plus sign lets you add an operation that the integration performs between the start and finish connections.',
        anchorId: 'integrations.addStep',
        placement: 'right',
      /*
      }, {
        route: '',
        title: 'Source Fields',
        content: 'List of fields in the data that is coming into this mapping step.',
        anchorId: 'mapper.source',
        placement: 'top',
      }, {
        route: '',
        title: 'Target Fields',
        content: 'List of fields in the data that leaves this mapping step to continue the integration.',
        anchorId: 'mapper.target',
        placement: 'top',
      }, {
        route: '',
        title: 'Data Mapper Tools',
        content: 'Toggles for displaying details, listing mappings and showing types, lines, mapped or unmapped fields.',
        anchorId: 'mapper.tools',
        placement: 'bottom',
      }, {
        route: '',
        title: 'Separate or Combine',
        content: 'Define a mapping that separates one source field into two target fields ' +
        'or combines two source fields into one target field.',
        anchorId: 'mapper.details',
        placement: 'top',
        */
      }, {
        route: 'integrations/create/integration-basics',
        title: 'Publish',
        content: 'Click Publish to start running the integration, which will take a moment or two. ' +
        'Click Save as Draft to save the integration without deploying it.',
        anchorId: 'integrations.publish',
        placement: 'bottom',
      }, {
        route: 'integrations', // /:id Any ID
        title: 'Edit',
        content: 'You can edit an integration even if it is deployed. Updates do not affect the running integration.',
        anchorId: 'integration.edit',
        placement: 'bottom',
      }, {
        route: 'integrations', // /:id Any ID
        title: 'History',
        content: 'Lists the versions of the integration. At any given moment, only one version can be running.',
        anchorId: 'integration.history',
        placement: 'top',
      }, {
        route: 'integrations', // /:id Any ID
        title: 'Delete',
        content: 'Removes all versions of the integration from Fuse Ignite.',
        anchorId: 'integration.delete',
        placement: 'top',
      }, {
        route: 'dashboard',
        title: 'Navigation',
        content: 'View integrations, connections or registration settings.',
        anchorId: 'dashboard.navigation',
        placement: 'right',
      } ],
      {
        route: '',
      },
    );
    this.tourService.start();
  }

  ngOnInit() {
    this.productBuild = this.config.getSettings(
      'branding',
      'productBuild',
      false,
    );
    this.logoWhiteBg = this.config.getSettings(
      'branding',
      'logoWhiteBg',
      'assets/images/syndesis-logo-svg-white.svg',
    );
    this.logoDarkBg = this.config.getSettings(
      'branding',
      'logoDarkBg',
      'assets/images/syndesis-logo-svg-white.svg',
    );
    this.iconDarkBg = this.config.getSettings(
      'branding',
      'iconDarkBg',
      'assets/images/glasses_logo_square.png',
    );
    this.iconWhiteBg = this.config.getSettings(
      'branding',
      'iconWhiteBg',
      'assets/images/glasses_logo_square.png',
    );
    const title = (this.title = this.config.getSettings(
      'branding',
      'appName',
      'Syndesis',
    ));
    document.title = title;
    const favicon32 = this.config.getSettings(
      'branding',
      'favicon32',
      '/favicon-32x32.png',
    );
    const favicon16 = this.config.getSettings(
      'branding',
      'favicon16',
      '/favicon-16x16.png',
    );
    const touchIcon = this.config.getSettings(
      'branding',
      'touchIcon',
      '/apple-touch-icon.png',
    );
    document.getElementById('favicon32').setAttribute('href', favicon32);
    document.getElementById('favicon16').setAttribute('href', favicon16);
    document.getElementById('appName').setAttribute('content', title);
    document.getElementById('appTitle').setAttribute('content', title);
    document.getElementById('touchIcon').setAttribute('href', touchIcon);
    this.user = this.userService.user;
    this.notifications = this.notificationService.getNotifications();
    this.showClose = true;
  }

  /**
   * Function that calls UserService to log the user out.
   */
  logout() {
    this.loggedIn = false;
    return this.userService.logout();
  }

  /**
   * Function that resets the database.
   */
  resetDB() {
    this.testSupport.resetDB().subscribe((value: Response) => {
      log.debugc(() => 'DB has been reset');
    });
  }

  /**
   * Function that exports the database.
   */
  exportDB() {
    this.testSupport.snapshotDB().subscribe((value: Response) => {
      const blob = new Blob([ value.text() ], {
        type: 'text/plain;charset=utf-8',
      });
      saveAs(blob, 'syndesis-db-export.json');
    });
  }

  /**
   * Function that displays a modal for importing a database.
   */
  showImportDB() {
    this.modalService.show('importDb').then(modal => {
      if (modal.result) {
        return this.testSupport
          .restoreDB(modal[ 'json' ])
          .take(1)
          .toPromise()
          .then(_ => log.debugc(() => 'DB has been imported'));
      }
    });
  }

  /**
   * Function that imports a database.
   */
  importDB(event, modal) {
    const file = event.srcElement.files[ 0 ];
    const reader = new FileReader();
    reader.onload = _ => (modal[ 'json' ] = JSON.parse(reader.result));
    reader.readAsText(file, 'text/plain;charset=utf-8');
  }

  handleAction($event: NotificationEvent): void {
    if ($event.action.id === 'reload') {
      location.reload();
    }
  }

  /**
   * Function that handles closing a PatternFly notification.
   */
  handleClose($event: NotificationEvent): void {
    this.notificationService.remove($event.notification);
  }

  ngAfterViewInit() {
    $(document).ready(function () {
      /**
       * On document ready, invoke jQuery's matchHeight method to adjust card height across app based
       * on contents of each .card-pf and then the .card-pf division itself.
       * This is applicable for layouts that utilize PatternFly's card view.
       */
      $(
        ".row-cards-pf > [class*='col'] > .card-pf .card-pf-title",
      ).matchHeight();
      $(
        ".row-cards-pf > [class*='col'] > .card-pf > .card-pf-body",
      ).matchHeight();
      $(
        ".row-cards-pf > [class*='col'] > .card-pf > .card-pf-footer",
      ).matchHeight();
      $(".row-cards-pf > [class*='col'] > .card-pf").matchHeight();
    });
    this.nav.initialize();
  }
}
