/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { DebugElement } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';

import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { NotificationModule } from 'patternfly-ng';
import { StoreModule } from '../../store/store.module';

import { SyndesisCommonModule } from '../../common/common.module';
import { ConnectionsListComponent } from './list.component';

describe('ConnectionsListComponent', () => {
  let component: ConnectionsListComponent;
  let fixture: ComponentFixture<ConnectionsListComponent>;

  beforeEach(
    async(() => {
      TestBed.configureTestingModule({
        imports: [
          CommonModule,
          SyndesisCommonModule.forRoot(),
          RouterTestingModule.withRoutes([]),
          ModalModule.forRoot(),
          BsDropdownModule.forRoot(),
          StoreModule,
          NotificationModule
        ],
        declarations: [ConnectionsListComponent]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
