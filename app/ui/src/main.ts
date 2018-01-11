import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { environment } from './environments/environment';
import { AppModule } from './app/app.module';

if (environment.production) {
  enableProdMode();
}

/* tslint:disable:no-console */
platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.log(err));
