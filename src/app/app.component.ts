import { Component, ChangeDetectionStrategy, OnInit, AfterViewInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { OAuthService } from 'angular-oauth2-oidc-hybrid';

import { log } from './logging';

import { UserService } from './common/user.service';
import { User } from './model';

@Component({
  selector: 'ipaas-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent implements OnInit, AfterViewInit {

  // White BG
  logoWhiteBg = 'assets/images/rh_ipaas_small.svg';
  iconWhiteBg = 'assets/images/glasses_logo.svg';

  // Dark BG
  logoDarkBg = 'assets/images/rh_ipaas_small.svg';
  iconDarkBg = 'assets/images/glasses_logo.svg';

  loggedIn = false;
  title = 'Red Hat iPaaS';
  url = 'https://www.twitter.com/jboss';
  user: Observable<User>;

  constructor(private oauthService: OAuthService, private userService: UserService) {
  }

  ngOnInit() {
    this.loggedIn = this.oauthService.hasValidAccessToken();
    this.user = this.userService.user;
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
