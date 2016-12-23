import { Component, ViewEncapsulation } from '@angular/core';

import { ConfigService } from './config.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
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

  constructor(private _config: ConfigService) { }

  ngOnInit() {
    console.log('Config', this._config.getSettings());
  }
}
