/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { MockBackend } from '@angular/http/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';

import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';

import { ConnectionViewComponent } from './view.component';
import { ConnectionsListComponent } from '../list/list.component';
import { ConnectionsListToolbarComponent } from '../list-toolbar/list-toolbar.component';
import { StoreModule } from '../../store/store.module';
import { IPaaSCommonModule } from '../../common/common.module';

describe('ConnectionViewComponent', () => {
  let component: ConnectionViewComponent;
  let fixture: ComponentFixture<ConnectionViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        CommonModule,
        FormsModule,
        IPaaSCommonModule,
        StoreModule,
        RouterTestingModule.withRoutes([]),
      ],
      declarations: [
        ConnectionViewComponent,
        ConnectionsListComponent,
        ConnectionsListToolbarComponent,
      ],
      providers: [
        MockBackend,
        { provide: RequestOptions, useClass: BaseRequestOptions },
        {
          provide: Http, useFactory: (backend, options) => {
            return new Http(backend, options);
          }, deps: [MockBackend, RequestOptions],
        },
      ],

      }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => { expect(component).toBeTruthy(); });
});
