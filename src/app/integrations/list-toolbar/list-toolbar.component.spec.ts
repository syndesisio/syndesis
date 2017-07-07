/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { IntegrationsListToolbarComponent } from './list-toolbar.component';

describe('IntegrationsListToolbarComponent', () => {
  let component: IntegrationsListToolbarComponent;
  let fixture: ComponentFixture<IntegrationsListToolbarComponent>;

  beforeEach(
    async(() => {
      TestBed.configureTestingModule({
        imports: [RouterTestingModule.withRoutes([])],
        declarations: [IntegrationsListToolbarComponent],
      }).compileComponents();
    }),
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(IntegrationsListToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
