/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';

import { StoreModule } from '@ngrx/store';

import { DashboardComponent } from './dashboard.component';
import { EmptyStateComponent } from './emptystate.component';
import { PopularTemplatesComponent } from './populartemplates.component';
import { TemplatesListComponent } from '../templates/list/list.component';
import { reducers } from '../store/store';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [StoreModule.provideStore(reducers), RouterTestingModule.withRoutes([])],
      declarations: [DashboardComponent, EmptyStateComponent, PopularTemplatesComponent, TemplatesListComponent],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
