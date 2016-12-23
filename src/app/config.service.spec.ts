/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { Http } from '@angular/http';
import { ConfigService } from './config.service';

describe('ConfigService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ConfigService, Http]
    });
  });

  it('should ...', inject([ConfigService], (service: ConfigService) => {
    expect(service).toBeTruthy();
  }));
});
