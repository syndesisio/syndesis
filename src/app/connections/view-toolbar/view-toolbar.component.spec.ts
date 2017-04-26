/* tslint:disable:no-unused-variable */
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { CommonModule } from '@angular/common';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { MockBackend } from '@angular/http/testing';

import { ConnectionViewToolbarComponent } from './view-toolbar.component';
import { StoreModule } from '../../store/store.module';
import { IPaaSCommonModule } from '../../common/common.module';
import { CurrentConnectionService } from '../create-page/current-connection';

describe('ConnectionViewToolbarComponent', () => {
  let component: ConnectionViewToolbarComponent;
  let fixture: ComponentFixture<ConnectionViewToolbarComponent>;

  beforeEach(async(() => {
    TestBed
      .configureTestingModule({
        imports: [
          CommonModule,
          RouterTestingModule.withRoutes([]),
          IPaaSCommonModule,
          StoreModule,
        ],
        declarations: [ConnectionViewToolbarComponent],
        providers: [
          MockBackend,
          CurrentConnectionService,
          { provide: RequestOptions, useClass: BaseRequestOptions },
          {
            provide: Http, useFactory: (backend, options) => {
              return new Http(backend, options);
            }, deps: [MockBackend, RequestOptions],
          },
        ],
      })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionViewToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => { expect(component).toBeTruthy(); });
});
