import { Injectable } from '@angular/core';
import { LeveledMessage, I18NService } from '@syndesis/ui/platform';

@Injectable()
export abstract class StatusCodeDecoderService {
  constructor(public i18NService: I18NService) {}
  abstract getMessageString(message: LeveledMessage, args?: any[]);
}
