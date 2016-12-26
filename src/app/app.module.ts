import { BrowserModule } from '@angular/platform-browser';
import { NgModule, APP_INITIALIZER } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule, Http } from '@angular/http';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { RestangularModule, Restangular } from 'ng2-restangular';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './approuting/approuting.module';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ConfigService, configServiceInitializer } from './config.service';

export function restangularProviderConfigurer(restangularProvider: any, config: ConfigService, ) {
  restangularProvider.setBaseUrl(config.getSettings().apiEndpoint);
}

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    RestangularModule.forRoot([ConfigService], restangularProviderConfigurer),
    NgbModule.forRoot(),
    AppRoutingModule
  ],
  providers: [
    ConfigService,
    {
      provide: APP_INITIALIZER,
      useFactory: configServiceInitializer,
      deps: [ConfigService],
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
