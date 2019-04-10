import { Subject } from 'rxjs';
import { ChangeEvent, EventsService } from '.';

export class EventsServiceMock {
  messageEvents = new Subject<String>();
  changeEvents = new Subject<ChangeEvent>();

  start() {
    // nothing to do...
  }
}

export const EVENTS_SERVICE_MOCK_PROVIDER = {
  provide: EventsService,
  useValue: new EventsServiceMock(),
};
