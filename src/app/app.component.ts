import { AfterViewInit, ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Response } from '@angular/http';
import { saveAs } from 'file-saver';
import { Restangular } from 'ngx-restangular';
import { Notification, NotificationEvent, NotificationService } from 'patternfly-ng';
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
  styleUrls: ['./app.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [Restangular, TestSupportService],
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

  constructor(
    private config: ConfigService,
    private userService: UserService,
    public testSupport: TestSupportService,
    private notificationService: NotificationService,
    private nav: NavigationService,
    private modalService: ModalService,
  ) {}

  ngOnInit() {
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
      const blob = new Blob([value.text()], {
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
          .restoreDB(modal['json'])
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
    const file = event.srcElement.files[0];
    const reader = new FileReader();
    reader.onload = _ => (modal['json'] = JSON.parse(reader.result));
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
    $(document).ready(function() {
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
