/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { IntegrationsListComponent } from './list.component';

describe('IntegrationsListComponent', () => {
  let component: IntegrationsListComponent;
  let fixture: ComponentFixture<IntegrationsListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [IntegrationsListComponent],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IntegrationsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
