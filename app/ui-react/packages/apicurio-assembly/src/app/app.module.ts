import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { NgModule } from '@angular/core';
import {
  ApicurioCommonComponentsModule,
  ApicurioEditorModule,
} from 'apicurio-design-studio';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { ModalModule } from 'ngx-bootstrap/modal';
import { AppComponent } from './app.component';
import { ApicurioHostComponent } from './apicurio-host.component';
import { WindowRef } from './WindowRef';

@NgModule({
  declarations: [AppComponent, ApicurioHostComponent],
  imports: [
    BrowserAnimationsModule,
    BrowserModule,
    CommonModule,
    FormsModule,
    ModalModule.forRoot(),
    BsDropdownModule.forRoot(),
    RouterModule.forRoot([]),
    ApicurioEditorModule,
    ApicurioCommonComponentsModule,
  ],
  providers: [WindowRef],
  bootstrap: [AppComponent],
})
export class AppModule {}
