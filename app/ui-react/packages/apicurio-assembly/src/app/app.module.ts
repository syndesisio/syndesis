import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { NgModule } from '@angular/core';
import {
  ApicurioCommonComponentsModule,
  ApicurioEditorModule,
} from 'apicurio-design-studio';
import { AppComponent } from './app.component';
import { ApicurioHostComponent } from './apicurio-host.component';
import { WindowRef } from './WindowRef';

@NgModule({
  declarations: [AppComponent, ApicurioHostComponent],
  imports: [
    BrowserModule,
    FormsModule,
    RouterModule.forRoot([]),
    ApicurioEditorModule,
    ApicurioCommonComponentsModule,
  ],
  providers: [WindowRef],
  bootstrap: [AppComponent],
})
export class AppModule {}
