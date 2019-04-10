import { AfterViewInit, Component, OnInit, Inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { Meta, Title } from '@angular/platform-browser';
import { saveAs } from 'file-saver';
import { Notification, NotificationEvent } from 'patternfly-ng';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';
import {
  UserService,
  User,
  PlatformActions,
  PlatformState,
} from '@syndesis/ui/platform';
import { ModalService } from '@syndesis/ui/common/modal/modal.service';
import { NavigationService } from '@syndesis/ui/common/navigation.service';
import { ConfigService } from '@syndesis/ui/config.service';
import { log } from '@syndesis/ui/logging';
import { TestSupportService } from '@syndesis/ui/store/test-support.service';
import { NotificationService } from '@syndesis/ui/common/ui-patternfly/notification-service';
import {
  Event,
  NavigationCancel,
  NavigationEnd,
  NavigationError,
  NavigationStart,
  Router,
} from '@angular/router';

@Component({
  selector: 'syndesis-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, AfterViewInit {
  helpExpanded = false;
  userMenuExpanded = false;
  menuExpanded = false;

  loggedIn = true;

  /**
   * @type {Observable<User>}
   * Observable instance of the active user
   */
  user$: Observable<User>;

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

  notifications: Observable<Notification[]>;

  /**
   * Local var used to determine whether or not to display a close
   * button on a PatternFly toast notification.
   */
  showClose: boolean;

  constructor(
    private store: Store<PlatformState>,
    private config: ConfigService,
    private userService: UserService,
    private testSupport: TestSupportService,
    private notificationService: NotificationService,
    private navigationService: NavigationService,
    private modalService: ModalService,
    private title: Title,
    private meta: Meta,
    private router: Router,
    @Inject(DOCUMENT) private document: any
  ) {}

  ngOnInit() {
    this.store.dispatch(new PlatformActions.AppBootstrap());

    this.appName = this.config.getSettings('branding', 'appName', 'Syndesis');
    this.title.setTitle(this.appName);
    this.meta.updateTag({ content: this.appName }, 'id="appName"');
    this.meta.updateTag({ content: this.appName }, 'id="appTitle"');

    this.productBuild = this.config.getSettings(
      'branding',
      'productBuild',
      false
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

    this.router.events.subscribe((value: Event) => {
      if (value instanceof NavigationStart) {
        log.debug('NavigationStart: ' + value.url);
      } else if (value instanceof NavigationEnd) {
        log.debug('NavigationEnd: ' + value.url);
      } else if (value instanceof NavigationCancel) {
        log.debug('NavigationCancel: ' + value.url);
      } else if (value instanceof NavigationError) {
        log.debug('NavigationError: ' + value.url);
      }
    });
    this.navigationService.collapsed$.subscribe(collapsed =>
      // This apparently can get triggered at a bad time for angular
      setTimeout(() => (this.menuExpanded = collapsed), 1)
    );
  }

  /**
   * Function that calls UserService to log the user out.
   */
  logout() {
    this.loggedIn = false;
    this.userService.logout();
  }

  /**
   * Function that resets the database.
   */
  resetDB() {
    this.testSupport
      .resetDB()
      .subscribe(() => log.debug(() => 'DB has been reset'));
  }

  /**
   * Function that exports the database.
   */
  exportDB() {
    this.testSupport.snapshotDB().subscribe((value: Blob) => {
      saveAs(value, 'syndesis-db-export.json');
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
          .subscribe(() => log.debug(() => 'DB has been imported'));
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
      window.location.reload();
    }
  }

  /**
   * Function that handles closing a PatternFly notification.
   */
  handleClose($event: NotificationEvent): void {
    this.notificationService.remove($event.notification);
  }

  /**
   * Toggles the help dropdown menu within the masthead
   */
  toggleHelpDropdown(): void {
    this.helpExpanded = this.helpExpanded ? false : true;
  }

  /**
   * Closes the help dropdown menu when clicking outside
   */
  closeHelpDropdown(): void {
    this.helpExpanded = false;
  }

  /**
   * Toggles the logout dropdown menu within the masthead
   */
  toggleUserMenuDropdown(): void {
    this.userMenuExpanded = this.userMenuExpanded ? false : true;
  }

  /**
   * Closes the logout dropdown when clicking outside
   */
  closeUserMenuDropdown(): void {
    this.userMenuExpanded = false;
  }

  /**
   * Expands and contracts the vertical navigation menu.
   */
  hamburgerToggle(): void {
    this.navigationService.toggle(!this.menuExpanded);
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
  }
}
