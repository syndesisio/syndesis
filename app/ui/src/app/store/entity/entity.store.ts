import {
  of as observableOf,
  merge as observableMerge,
  Observable,
  BehaviorSubject,
  Subscription,
  Subject
} from 'rxjs';

import { share, mergeMap, filter, debounceTime } from 'rxjs/operators';
import { plural } from 'pluralize';

import { BaseEntity } from '@syndesis/ui/platform';
import { RESTService } from '@syndesis/ui/store/entity/rest.service';

import { log, getCategory } from '@syndesis/ui/logging';
import {
  EventsService,
  ChangeEvent
} from '@syndesis/ui/store/entity/events.service';

const category = getCategory('AbstractStore');

// Set to a number of seconds to simulate latency and do page styling for loading states
const LOADING_TIME = 0;

// Set to true to simulate empty state
const EMPTY_STATE = false;

// prettier-ignore
export abstract class AbstractStore<
  T extends BaseEntity,
  L extends Array<T>,
  R extends RESTService<T, L>> {
  private _list: BehaviorSubject<L>;

  private _current: BehaviorSubject<T>;
  private currentId: string;

  private _loading: BehaviorSubject<boolean> = new BehaviorSubject(false);

  private changeEvents: Observable<ChangeEvent>;
  private listSubscription: Subscription;
  private currentSubscription: Subscription;

  constructor(
    public service: R,
    private eventService: EventsService,
    initialList: L,
    initialCurrent: T,
  ) {
    // TODO: trigger event service to start in case it hasn't, though it should
    this.eventService.start();
    this._list = new BehaviorSubject<L>(initialList);
    this._current = new BehaviorSubject<T>(initialCurrent);
    this.changeEvents = this.setChangeEventsFilter(this.eventService.changeEvents);
    this._current.asObservable().subscribe(current => {
      if (!current) {
        this.currentId = undefined;
        return;
      }
      this.currentId = current.id;
    });
  }

  setChangeEventsFilter(changeEvents: Subject<ChangeEvent>) {
    return changeEvents.pipe(filter(event => event.kind === this.service.kind));
  }

  protected abstract get kind(): string;

  get list() {
    // Give back the _list,
    // but also update it if we get notified the a change occurred.
    return this._list.pipe(share());
  }

  get resource() {
    return observableMerge(
      this._current,
      this.changeEvents.pipe(
        filter(event => {
          return event.id.startsWith(this.currentId);
        }),
        mergeMap(event => {
          return this.service.get(this.currentId);
        })
      )
    ).pipe(share());
  }

  get loading() {
    return this._loading.pipe(share());
  }

  loadAll(retries = 0) {
    if (!retries && this.listSubscription) {
      return;
    } else {
      // forcing a reload in this case
      if (this.listSubscription) {
        this.listSubscription.unsubscribe();
      }
    }
    this._loading.next(true);
    this.listSubscription = observableMerge(
      this.service.list(),
      this.changeEvents.pipe(
        debounceTime(500),
        mergeMap(event => {
        // We could probably get fancy one day an only fetch the entry that matches event.id
        // simulate no data
        if (EMPTY_STATE) {
          return observableOf([] as L) as Observable<L>;
        } else {
          return this.service.list();
        }
      }))).subscribe(
      list => {
        setTimeout(() => {
          // simulate no elements
          if (EMPTY_STATE) {
            this._list.next(<L>[]);
          } else {
            this._list.next(list);
          }
          this._loading.next(false);
        }, LOADING_TIME);
      },
      error => {
        error = this.massageError(error);
        log.debug(
          () => 'Error retrieving ' + plural(this.kind) + ': ' + error,
          category,
        );
        if (retries < 3) {
          setTimeout(() => {
            this.loadAll(retries + 1);
          }, (retries + 1) * 1000);
        } else {
          this._list.error(error);
          this._loading.next(false);
        }
      });
  }

  newInstance(): T {
    throw new Error('No `newInstance()` defined for ' + this.kind);
  }

  loadOrCreate(id?: string) {
    if (id) {
      return this.load(id);
    } else {
      // This should happen async too for consistency
      setTimeout(() => {
        this._current.next(this.newInstance());
        this._loading.next(false);
      }, 1);
      return this._current.pipe(share());
    }
  }

  // Use clear() to clear the current resource so views don't show stale data
  clear(): void {
    this._current.next(undefined);
    this._loading.next(true);
  }

  load(id: string, retries = 0): Observable<T> {
    this._loading.next(true);
    if (this.currentSubscription) {
      this.currentSubscription.unsubscribe();
    }
    this.currentSubscription = this.service.get(id).subscribe(
      entity => {
        setTimeout(() => {
          this._current.next(entity);
          this._loading.next(false);
        }, LOADING_TIME);
      },
      error => {
        error = this.massageError(error);
        log.debug(
          () => 'Error retrieving ' + this.kind + ': ' + error,
          category,
        );
        // don't retry if the entity isn't there
        if (error.status !== 404 && retries < 3) {
          setTimeout(() => {
            this.load(id, retries + 1);
          }, (retries + 1) * 1000);
        } else {
          this._current.error(error);
          this._loading.next(false);
        }
      },
    );
    return this._current.pipe(share());
  }

  create(entity: T): Observable<T> {
    const created = new Subject<T>();
    this.service.create(entity).subscribe(
      e => {
        created.next(e);
      },
      error => {
        error = this.massageError(error);
        log.debug(
          () =>
            'Error creating ' +
            this.kind +
            ' (' +
            JSON.stringify(entity, null, 2) +
            ')' +
            ': ' +
            error,
          category,
        );
        created.error(error);
      },
    );
    return created.pipe(share());
  }

  update(entity: T, reload = false): Observable<T> {
    if (reload) {
      this._loading.next(true);
    }
    const updated = new Subject<T>();
    this.service.update(entity).subscribe(
      e => {
        // PUT may not return a response, we'll massage it
        if (Array.isArray(e)) {
          e = entity;
        }
        updated.next(e);
        this._loading.next(false);
      },
      error => {
        error = this.massageError(error);
        log.debug(
          () =>
            'Error updating ' +
            this.kind +
            ' (' +
            JSON.stringify(entity, null, 2) +
            ')' +
            ': ' +
            error,
          category,
        );
        updated.error(error);
        this._loading.next(false);
      },
    );
    return updated.pipe(share());
  }

  updateOrCreate(entity: T): Observable<T> {
    if (entity.id) {
      return this.update(entity);
    } else {
      return this.create(entity);
    }
  }

  delete(entity: T): Observable<T> {
    const deleted = new Subject<T>();
    this.service.delete(entity).subscribe(
      e => {
        if (e === null) {
          e = entity;
        }
        deleted.next(e);
      },
      error => {
        error = this.massageError(error);
        log.debug(
          () =>
            'Error updating ' +
            this.kind +
            ' (' +
            JSON.stringify(entity, null, 2) +
            ')' +
            ': ' +
            error,
          category,
        );
        deleted.error(error);
      },
    );
    return deleted.pipe(share());
  }

  patch(id: string, attributes: any): Observable<any> {
    return this.service.patch(id, attributes);
  }

  private massageError(error: any) {
    let errorMessage: any;
    switch (typeof error) {
      case 'object':
        if (error['data'] && error['data'] instanceof Array) {
          errorMessage = error['data'].map(e => e.message).join(' ');
        } else {
          errorMessage = error;
        }
        break;
      case 'string':
        try {
          errorMessage = JSON.parse(error);
        } catch (err) {
          // some random text back from the server :-(
          errorMessage = { error: error };
        }
        break;
      default:
        errorMessage = { error: error };
    }

    return errorMessage;
  }
}
