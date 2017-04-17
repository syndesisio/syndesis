/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { DebugElement } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';

import { ModalModule } from 'ng2-bootstrap/modal';
import { DropdownModule } from 'ng2-bootstrap/dropdown';
import { ToasterModule } from 'angular2-toaster';
import { StoreModule } from '../../store/store.module';

import { IPaaSCommonModule } from '../../common/common.module';
import { ConnectionsListComponent } from './list.component';

describe('ConnectionsListComponent', () => {
  let component: ConnectionsListComponent;
  let fixture: ComponentFixture<ConnectionsListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        CommonModule,
        IPaaSCommonModule,
        RouterTestingModule.withRoutes([]),
        ModalModule.forRoot(),
        DropdownModule.forRoot(),
        ToasterModule,
        StoreModule,
      ],
      declarations: [ConnectionsListComponent],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
