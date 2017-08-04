/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ToolbarModule } from 'patternfly-ng';

import { ConnectionsListToolbarComponent } from './list-toolbar.component';

describe('ConnectionsListToolbarComponent', () => {
  let component: ConnectionsListToolbarComponent<any>;
  let fixture: ComponentFixture<ConnectionsListToolbarComponent<any>>;

  beforeEach(
    async(() => {
      TestBed.configureTestingModule({
        imports: [
          RouterTestingModule.withRoutes([]),
          ToolbarModule,
        ],
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
