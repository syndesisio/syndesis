import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { DataMapperModule } from '@atlasmap/atlasmap-data-mapper';
import { NgModule } from '@angular/core';
import { AppComponent } from './app.component';
import { DataMapperHostComponent } from './data-mapper-host.component';

@NgModule({
  declarations: [AppComponent, DataMapperHostComponent],
  imports: [
    BrowserModule,
    FormsModule,
    RouterModule.forRoot([]),
    DataMapperModule.withInterceptor(),
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
