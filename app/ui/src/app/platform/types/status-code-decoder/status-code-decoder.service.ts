import { Injectable } from '@angular/core';
import { LeveledMessage, StringMap } from '@syndesis/ui/platform';

@Injectable()
export abstract class StatusCodeDecoderService {
  abstract getMessageString(message: LeveledMessage, context?: StringMap<any>);
}
