import { Pipe, PipeTransform } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators';

import { moment } from '@syndesis/ui/vendor';

@Pipe({
  name: 'synDuration'
})
export class DurationPipe implements PipeTransform {
  transform(duration: number, timeUnit: 's' | 'm' | 'h' | 'd' | 'w'): string {
    if (!duration) {
      return 'NaN';
    }

    const durationMoment = moment.duration(duration);
    switch (timeUnit.toLowerCase()) {
      case 'm': {
        return `${durationMoment.minutes()} minutes`;
      }

      case 'h': {
        return `${durationMoment.hours()} hours`;
      }

      case 'd': {
        return `${durationMoment.days()} days`;
      }

      case 'w': {
        return `${durationMoment.weeks()} weeks`;
      }

      default: {
        return `${durationMoment.seconds()} seconds`;
      }
    }
  }
}
