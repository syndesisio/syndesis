import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  AfterViewInit,
  ViewChild,
} from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Restangular } from 'ngx-restangular';
import { OAuthService } from 'angular-oauth2-oidc-hybrid';
import { Response } from '@angular/http';
import { Notification, NotificationEvent, NotificationService } from 'patternfly-ng';

import { TestSupportService } from './store/test-support.service';

import { log } from './logging';

import { ModalService } from './common/modal/modal.service';
import { NavigationService } from './common/navigation.service';
import { UserService } from './common/user.service';
import { User } from './model';
import { saveAs } from 'file-saver';

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

  /**
   * Logo with dark background
   */
  logoDarkBg = 'assets/images/syndesis-logo-svg-white.svg';

  loggedIn = false;

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
    private oauthService: OAuthService,
    private userService: UserService,
    public testSupport: TestSupportService,
    private notificationService: NotificationService,
    private nav: NavigationService,
    private modalService: ModalService,
  ) {}

  ngOnInit() {
    this.loggedIn = this.oauthService.hasValidAccessToken();
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
    this.modalService.show('importDb')
      .then(modal => {
        if (modal.result) {
          return this.testSupport.restoreDB(modal['json'])
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
    reader.onload = _ => modal['json'] = JSON.parse(reader.result);
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
