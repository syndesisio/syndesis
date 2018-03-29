import { Injectable } from '@angular/core';
import { StatusCodeDecoderService, MessageCode, LeveledMessage, StringMap } from '@syndesis/ui/platform';

@Injectable()
export class StatusCodeDecoderProviderService extends StatusCodeDecoderService {

  // TODO eventually this would be in a separate json file, also better messages
  private static messages = {
    'SYNDESIS000': 'An unknown error has occurred', // generic message
    'SYNDESIS001': 'There are parameter updates for this connection. Click <strong>Edit</strong> to update parameter settings.',
    'SYNDESIS002': 'One or more properties have been updated or removed', // One or more properties have been added or removed
    'SYNDESIS003': 'The connector associated with this connection has been deleted', // Connector has been deleted
    'SYNDESIS004': 'The associated extension has been deleted', // Extension has been deleted
    'SYNDESIS005': 'The associated action has been deleted', // Action has been deleted
    'SYNDESIS006': 'One or more required properties is not set', // One or more required properties is not set
    'SYNDESIS007': 'Secrets for this connection need to be updated', // Secrets update needed
    'SYNDESIS008': 'There has a validation error', // Validation Error
  } as StringMap<string>;

  // TODO use 'context' for variable substitution in message string
  getMessageString(message: LeveledMessage, context?: StringMap<any>) {
    if (message.message) {
      return message.message;
    }
    const answer = StatusCodeDecoderProviderService.messages[message.code];
    return answer ? answer : 'An unknown error has occurred and no specific message corresponding to ' + message.code + ' has been set';
  }
}
