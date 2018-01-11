import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  OnInit,
  Inject
} from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { Response } from '@angular/http';
import { Meta, Title } from '@angular/platform-browser';
import { saveAs } from 'file-saver';
import { Restangular } from 'ngx-restangular';
import { Notification, NotificationEvent } from 'patternfly-ng';
import { Observable } from 'rxjs/Observable';
import { ModalService } from './common/modal/modal.service';
import { NavigationService } from './common/navigation.service';
import { UserService } from '@syndesis/ui/platform';
import { ConfigService } from './config.service';
import { log } from './logging';
import { User } from './model';
import { TestSupportService } from './store/test-support.service';
import { TourService } from 'ngx-tour-ngx-bootstrap';
import { NotificationService } from '@syndesis/ui/common/ui-patternfly/notification-service';

@Component({
  selector: 'syndesis-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  changeDetection: ChangeDetectionStrategy.Default,
  providers: [Restangular, TestSupportService]
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
   * @type {Observable<User>}
   * Observable instance of the active user
   */
  user$: Observable<User>;

  /**
   * @type {boolean}
   * Flag used to determine whether or not the user is logged in.
   */
  loggedIn = true;

  /**
   * @type {string}
   * Brand name for the application. Populates the browser title tag, amongst other uses.
   */
  appName = 'Syndesis';

  /**
   * @type {boolean}
   * Flag used to determine whether or not the user is a first time user.
   */
  firstTime = false;

  productBuild = false;

  /* tslint:disable */
  tutorialLink = 'https://access.redhat.com/documentation/en-us/red_hat_jboss_fuse/7.0-tp/html-single/fuse_ignite_sample_integration_tutorials/';
  userGuideLink = 'https://access.redhat.com/documentation/en-us/red_hat_jboss_fuse/7.0-tp/html-single/integrating_applications_with_ignite/';
  /* tslint:enable */

  /**
   * Guided Tour status
   */
  guidedTourStatus = true;

  notifications: Observable<Notification[]>;

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
    private navigationService: NavigationService,
    public tourService: TourService,
    private modalService: ModalService,
    private title: Title,
    private meta: Meta,
    @Inject(DOCUMENT) private document: any
  ) {}

  ngOnInit() {
    this.appName = this.config.getSettings('branding', 'appName', 'Syndesis');
    this.title.setTitle(this.appName);
    this.meta.updateTag({ content: this.appName }, 'id="appName"');
    this.meta.updateTag({ content: this.appName }, 'id="appTitle"');

    this.productBuild = this.config.getSettings(
      'branding',
      'productBuild',
      false
    );
    this.logoWhiteBg = this.config.getSettings(
      'branding',
      'logoWhiteBg',
      'assets/images/syndesis-logo-svg-white.svg'
    );
    this.logoDarkBg = this.config.getSettings(
      'branding',
      'logoDarkBg',
      'assets/images/syndesis-logo-svg-white.svg'
    );
    this.iconDarkBg = this.config.getSettings(
      'branding',
      'iconDarkBg',
      'assets/images/glasses_logo_square.png'
    );
    this.iconWhiteBg = this.config.getSettings(
      'branding',
      'iconWhiteBg',
      'assets/images/glasses_logo_square.png'
    );
    const favicon32 = this.config.getSettings(
      'branding',
      'favicon32',
      '/favicon-32x32.png'
    );
    const favicon16 = this.config.getSettings(
      'branding',
      'favicon16',
      '/favicon-16x16.png'
    );
    const touchIcon = this.config.getSettings(
      'branding',
      'touchIcon',
      '/apple-touch-icon.png'
    );

    if (this.document && this.document.getElementById) {
      this.document.getElementById('favicon32').setAttribute('href', favicon32);
      this.document.getElementById('favicon16').setAttribute('href', favicon16);
      this.document.getElementById('touchIcon').setAttribute('href', touchIcon);
    }

    this.user$ = this.userService.user;

    this.notifications = this.notificationService.getNotificationsObservable();
    this.showClose = true;
  }

  /**
   * Guided Tour
   */
  getTourState() {
    this.guidedTourStatus = this.userService.getTourState();
    return this.guidedTourStatus;
  }

  startTour() {
    this.tourService.start();
    this.userService.setTourState(true);
    this.guidedTourStatus = true;
  }

  endTour() {
    if (!!this.guidedTourStatus) {
      this.tourService.end();
      this.userService.setTourState(false);
      this.guidedTourStatus = false;
    }
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
      const blob = new Blob([value.text()], {
        type: 'text/plain;charset=utf-8'
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
    /**
     * On document ready, invoke jQuery's matchHeight method to adjust card height across app based
     * on contents of each .card-pf and then the .card-pf division itself.
     * This is applicable for layouts that utilize PatternFly's card view.
     * @TODO: Replace by a CSS-driven workaround based on Flexbox and then remove the jQuery dep
     */
    const $patternFlyCards = $('.row-cards-pf > [class*="col"] > .card-pf');
    $patternFlyCards.find('> .card-pf-body').matchHeight();
    $patternFlyCards.find('> .card-pf-footer').matchHeight();
    $patternFlyCards.find('.card-pf-title').matchHeight();
    $patternFlyCards.matchHeight();

    this.navigationService.initialize();
    this.userService.setTourState(true);
    this.guidedTourStatus = this.userService.getTourState();
  }
}
