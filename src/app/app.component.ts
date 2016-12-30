import { Component, ElementRef, ChangeDetectionStrategy } from '@angular/core';

import { ConfigService } from './config.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {
  name = 'Red Hat iPaaS';

  // White BG
  logoWhiteBg = 'assets/images/rh_ipaas_small.svg';
  iconWhiteBg = 'assets/images/glasses_logo.svg';

  // Dark BG
  logoDarkBg = 'assets/images/rh_ipaas_small.svg';
  iconDarkBg = 'assets/images/glasses_logo.svg';

  title = 'Red Hat iPaaS';
  url = 'https://www.twitter.com/jboss';
  loggedIn = true;

  constructor(private _config: ConfigService, private _elementRef: ElementRef) { }

  ngOnInit() {
    console.log('Config', this._config.getSettings());
  }

  ngAfterViewInit() {
    $(document).ready(function () {
      // matchHeight the contents of each .card-pf and then the .card-pf itself
      $(".row-cards-pf > [class*='col'] > .card-pf .card-pf-title").matchHeight();
      $(".row-cards-pf > [class*='col'] > .card-pf > .card-pf-body").matchHeight();
      $(".row-cards-pf > [class*='col'] > .card-pf > .card-pf-footer").matchHeight();
      $(".row-cards-pf > [class*='col'] > .card-pf").matchHeight();

      // Initialize the vertical navigation
      $().setupVerticalNavigation(true);
    });
  }
}
