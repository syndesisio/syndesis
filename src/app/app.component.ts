import { Component, ChangeDetectionStrategy, OnInit, AfterViewInit, ViewChild } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Restangular } from 'ngx-restangular';
import { OAuthService } from 'angular-oauth2-oidc-hybrid';
import { Response } from '@angular/http';

import { TestSupportService } from './store/test-support.service';

import { log } from './logging';

import { UserService } from './common/user.service';
import { User } from './model';
import { saveAs } from 'file-saver';
import { ModalDirective } from 'ngx-bootstrap';

@Component({
  selector: 'syndesis-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [ Restangular, TestSupportService ],
})
export class AppComponent implements OnInit, AfterViewInit {

  @ViewChild('importDBModal') public importDBModal: ModalDirective;

  // White BG
  logoWhiteBg = 'assets/images/syndesis-logo-svg-white.svg';
  iconWhiteBg = 'assets/images/glasses_logo.svg';

  // Dark BG
  logoDarkBg = 'assets/images/syndesis-logo-svg-white.svg';
  iconDarkBg = 'assets/images/glasses_logo.svg';

  loggedIn = false;
  title = 'Syndesis';
  url = 'https://www.twitter.com/jboss';
  user: Observable<User>;

  constructor(
    private oauthService: OAuthService,
    private userService: UserService,
    public testSupport: TestSupportService,
    ) {
  }

  ngOnInit() {
    this.loggedIn = this.oauthService.hasValidAccessToken();
    this.user = this.userService.user;
  }

  resetDB() {
    this.testSupport.resetDB().subscribe((value: Response) => {
      log.debugc(() => 'DB has been reset');
    });
  }

  exportDB() {
    this.testSupport.snapshotDB().subscribe((value: Response) => {
      const blob = new Blob([value.text()], {type: 'text/plain;charset=utf-8'});
      saveAs(blob, 'syndesis-db-export.json');
    });
  }

  showImportDB() {
    this.importDBModal.show();
  }

  hideModal() {
    this.importDBModal.hide();
  }

  importDB(event) {
    const file = event.srcElement.files[0];
    const reader = new FileReader();
    reader.onload = () => {
      const json = JSON.parse(reader.result);
      this.testSupport.restoreDB(json).subscribe((value: Response) => {
        log.debugc(() => 'DB has been imported');
      });
    };
    reader.readAsText(file, 'text/plain;charset=utf-8');
    this.importDBModal.hide();
  }

  logout() {
    // TODO
  }

  ngAfterViewInit() {
    $(document).ready(function () {
      // matchHeight the contents of each .card-pf and then the .card-pf itself
      $(".row-cards-pf > [class*='col'] > .card-pf .card-pf-title").matchHeight();
      $(".row-cards-pf > [class*='col'] > .card-pf > .card-pf-body").matchHeight();
      $(".row-cards-pf > [class*='col'] > .card-pf > .card-pf-footer").matchHeight();
      $(".row-cards-pf > [class*='col'] > .card-pf").matchHeight();
    });
    $.fn.setupVerticalNavigation ? $.fn.setupVerticalNavigation() : '';
  }

}
