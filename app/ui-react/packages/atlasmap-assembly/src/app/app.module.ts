import { HttpClientModule, HttpClientXsrfModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import {
  ApiHttpXsrfTokenExtractor,
  DataMapperModule,
} from '@atlasmap/atlasmap-data-mapper';
import { NgModule } from '@angular/core';
import { environment } from '../environments/environment';
import { AppComponent } from './app.component';
import { DataMapperHostComponent } from './data-mapper-host.component';

@NgModule({
  declarations: [AppComponent, DataMapperHostComponent],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    HttpClientXsrfModule.withOptions(environment.xsrf),
    RouterModule.forRoot([]),
    DataMapperModule,
  ],
  providers: [ApiHttpXsrfTokenExtractor],
  bootstrap: [AppComponent],
})
export class AppModule {}
