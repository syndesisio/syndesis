import { NgModule } from '@angular/core';
import { HttpModule } from '@angular/http';
import { RestangularModule } from 'ngx-restangular';
import { IntegrationSupportService } from './integration-support.service';

@NgModule({
  imports: [
    HttpModule
  ],
  providers: [
    IntegrationSupportService
  ]
})
export class IntegrationSupportModule {}
