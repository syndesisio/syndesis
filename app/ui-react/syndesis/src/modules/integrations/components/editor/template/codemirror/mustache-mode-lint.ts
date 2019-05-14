import { CodeMirror, TemplateType } from '@syndesis/ui';
import { Mustache } from '@syndesis/utils';
import i18n from '../../../../../../i18n';
import { AbstractLanguageLint } from './abstract-language-lint';
import { TemplateSymbol } from './template-symbol';

export class MustacheModeLint extends AbstractLanguageLint {
  constructor() {
    super(TemplateType.Mustache);
  }

  public parse(content: string): any[] {
    const symbols: TemplateSymbol[] = [];
    const tokens: any[] = Mustache.parse(content);
    Mustache.clearCache();
    for (const token of tokens) {
      if (token[0] === 'text' || token[0] === '!') {
        continue;
      }
      if (token[0] === 'name') {
        symbols.push(new TemplateSymbol(token[1], 'string'));
      }
    }
    return symbols;
  }

  protected define(): void {
    CodeMirror.defineMode(this.name(), (config, parserConfig) => {
      return {
        token: (stream, state) => {
          let ch;
          if (stream.match('{{')) {
            // tslint:disable-next-line
            while ((ch = stream.next()) != null) {
              if (ch === '}' && stream.next() === '}') {
                stream.eat('}');
                return 'mustache';
              }
            }
          }
          while (stream.next() != null && !stream.match('{{', false)) {
            // Read it but don't do anything
          }
          return null;
        },
      };
    });
    super.define();
  }

  protected validate(text: string, errors: any[]): void {
    const symRegex = /^[A-Za-z_]+$/g;
    const format = '{{xyz}}';
    let line = 0;
    let startCol = 0;
    let endCol = 0;
    let openSymbol = 0;
    let closeSymbol = 0;
    let haveSymbol = false;
    let theSymbol = '';
    let reset = false;
    for (let i = 0; i < text.length; i++) {
      // Increase the column count
      endCol++;
      startCol = endCol - 1;
      if (reset) {
        // Successfully parsed a symbol so reset for next
        openSymbol = 0;
        closeSymbol = 0;
        theSymbol = '';
      }
      const ch = text.charAt(i);
      if (ch === '{') {
        openSymbol++;
        if (closeSymbol > 0) {
          // Found an open symbol before all close symbols
          const msg = i18n.t(
            'integrations:steps.templater-illegal-open-symbol',
            [line, endCol]
          );
          errors.push({
            from: CodeMirror.Pos(line, startCol),
            message: msg,
            severity: 'error',
            to: CodeMirror.Pos(line, endCol),
          });
          reset = true;
          continue;
        }
        if (openSymbol > 2) {
          // Too many open symbols encountered
          const msg = i18n.t(
            'integrations:steps.templater-too-many-open-symbols',
            [line, endCol]
          );
          errors.push({
            from: CodeMirror.Pos(line, startCol),
            message: msg,
            severity: 'error',
            to: CodeMirror.Pos(line, endCol),
          });
          reset = true;
          continue;
        }
      } else if (ch === '}') {
        closeSymbol++;
        if (openSymbol < 2) {
          // Found a close symbol before all the open symbols
          const msg = i18n.t(
            'integrations:steps.templater-illegal-close-symbol',
            [line, endCol]
          );
          errors.push({
            from: CodeMirror.Pos(line, startCol),
            message: msg,
            severity: 'error',
            to: CodeMirror.Pos(line, endCol),
          });
          reset = true;
          continue;
        }
        if (closeSymbol > 2) {
          // Too many close symbols encountered
          const msg = i18n.t(
            'integrations:steps.templater-too-many-close-symbols',
            [line, endCol]
          );
          errors.push({
            from: CodeMirror.Pos(line, startCol),
            message: msg,
            severity: 'error',
            to: CodeMirror.Pos(line, endCol),
          });
          reset = true;
          continue;
        }
      } else {
        //
        // Handle all other types of character
        //
        // Record the symbol text for checking
        if (openSymbol === 2 && closeSymbol === 0) {
          theSymbol = theSymbol + ch;
        }
        if (openSymbol === 1) {
          // Should have encountered another open symbol but not
          const msg = i18n.t(
            'integrations:steps.templater-expected-open-symbol',
            [line, endCol]
          );
          errors.push({
            from: CodeMirror.Pos(line, startCol),
            message: msg,
            severity: 'error',
            to: CodeMirror.Pos(line, endCol),
          });
          reset = true;
          continue;
        }

        if (closeSymbol === 1) {
          // Should have encountered another close symbol but not
          const msg = i18n.t(
            'integrations:steps.templater-expected-close-symbol',
            [line, endCol]
          );
          errors.push({
            from: CodeMirror.Pos(line, startCol),
            message: msg,
            severity: 'error',
            to: CodeMirror.Pos(line, endCol),
          });
          reset = true;
          continue;
        }

        if (ch === '\n') {
          // Encountered a carriage return so increment line and reset end column
          line++;
          endCol = 0;
        }
      }

      reset = openSymbol === 2 && closeSymbol === 2;
      if (reset) {
        // Text contains at least 1 parseable symbol so at least the text is not just constants
        haveSymbol = true;

        // Check the symbol conforms to the expected format
        if (theSymbol.length > 0 && !theSymbol.match(symRegex)) {
          const msg = i18n.t(
            'integrations:steps.templater-wrong-symbol-format',
            ['{{' + theSymbol + '}}', format, line, endCol]
          );
          errors.push({
            from: CodeMirror.Pos(line, startCol),
            message: msg,
            severity: 'error',
            to: CodeMirror.Pos(line, endCol),
          });
        }
      }
    }

    if (!haveSymbol) {
      const msg = i18n.t('integrations:steps.templater-no-symbols');
      errors.push({
        from: CodeMirror.Pos(0, 0),
        message: msg,
        severity: 'warning',
        to: CodeMirror.Pos(0, 0),
      });
    }
  }
}
