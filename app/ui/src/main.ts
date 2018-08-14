import { LOCALE_ID } from '@angular/core';
import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { environment } from 'environments/environment';
import { AppModule } from 'app';

if (environment.production) {
  enableProdMode();
}

/* tslint:disable:no-console */
platformBrowserDynamic().bootstrapModule(AppModule, {
  preserveWhitespaces: true,
  providers: [{provide: LOCALE_ID, useValue: 'en-US' }]
})
.catch(err => console.log(err));
