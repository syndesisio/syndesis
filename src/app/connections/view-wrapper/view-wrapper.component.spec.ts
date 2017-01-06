/* tslint:disable:no-unused-variable */
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {DebugElement} from '@angular/core';
import {RouterTestingModule} from '@angular/router/testing';
import {StoreModule} from '@ngrx/store';

import {ConnectionViewWrapperComponent} from './view-wrapper.component';
import {
  ConnectionViewToolbarComponent,
} from '../view-toolbar/view-toolbar.component';
import {ConnectionViewComponent} from '../view/view.component';
import {reducers} from '../../store/store';

describe('ConnectionViewWrapperComponent', () => {
  let component: ConnectionViewWrapperComponent;
  let fixture: ComponentFixture<ConnectionViewWrapperComponent>;

  beforeEach(async(() => {
    TestBed
        .configureTestingModule({
          imports: [
            StoreModule.provideStore(reducers),
            RouterTestingModule.withRoutes([]),
          ],
          declarations: [
            ConnectionViewWrapperComponent,
            ConnectionViewToolbarComponent,
            ConnectionViewComponent,
          ],
        })
        .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionViewWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => { expect(component).toBeTruthy(); });
});
