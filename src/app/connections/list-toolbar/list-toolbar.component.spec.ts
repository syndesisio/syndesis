/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { ConnectionsListToolbarComponent } from './list-toolbar.component';

describe('ConnectionsListToolbarComponent', () => {
  let component: ConnectionsListToolbarComponent;
  let fixture: ComponentFixture<ConnectionsListToolbarComponent>;

  beforeEach(
    async(() => {
      TestBed.configureTestingModule({
        imports: [RouterTestingModule.withRoutes([])],
        declarations: [ConnectionsListToolbarComponent],
      }).compileComponents();
    }),
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionsListToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
