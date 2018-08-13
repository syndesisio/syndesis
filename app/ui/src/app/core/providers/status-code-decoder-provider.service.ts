import { Injectable } from '@angular/core';
import {
  StatusCodeDecoderService,
  LeveledMessage,
  I18NService
} from '@syndesis/ui/platform';
import { environment } from 'environments/environment';
const { fallbackValue } = environment.i18n;

@Injectable()
export class StatusCodeDecoderProviderService extends StatusCodeDecoderService {
  constructor(public i18NService: I18NService) {
    super(i18NService);
  }

  getMessageString(message: LeveledMessage, args?: any[]) {
    if (message.message) {
      return message.message;
    }
    const answer = this.i18NService.localize('errors.' + message.code, args);
    return !answer || answer === fallbackValue
      ? 'An unknown error has occurred and no specific message corresponding to ' +
          message.code +
          ' has been set'
      : answer;
  }
}
