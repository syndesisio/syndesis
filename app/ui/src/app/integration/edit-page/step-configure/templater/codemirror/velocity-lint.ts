import {
  Injectable
} from '@angular/core';
import {
  I18NService
} from '@syndesis/ui/platform';
import {
  CodeMirror,
  Velocity
} from '@syndesis/ui/vendor';
import { AbstractLanguageLint } from './abstract-language-lint';
import { TemplateSymbol } from './template-symbol';

@Injectable()
export class VelocityLint extends AbstractLanguageLint {

  constructor(protected i18NService: I18NService) {
    super('velocity', i18NService);
  }

  public parse(content: string): TemplateSymbol[] {
    const symbols: TemplateSymbol[] = [];

    const tokens: any[] = Velocity.parse(content);

    for (const token of tokens) {
      if (token.type === 'references') {
        symbols.push(new TemplateSymbol(token.id, 'string'));
      }
    }
    return symbols;
  }

  protected validate(text: string, errors: any[]): void {
    try {
      const tokens: any[] = Velocity.parse(text);
      let totalSymbols = 0;
      for (const token of tokens) {
        if (token.type === 'references') {
          totalSymbols++;
        }
      }

      if (totalSymbols === 0) {
        const msg = this.i18NService.localize('integrations.steps.templater-no-symbols');
        errors.push({
          message: msg,
          severity: 'warning',
          from: CodeMirror.Pos(0, 0),
          to: CodeMirror.Pos(0, 0)
        });
      }

    } catch (exception) {
      const msg = exception.message;
      let startLine = 0;
      let endLine = 0;
      let startCol = 0;
      let endCol = 0;

      if (exception.hash) {
        const hash = exception.hash;
        if (hash.loc) {
          startLine = hash.loc.first_line > 0 ? hash.loc.first_line - 1 : 0;
          endLine = hash.loc.last_line > 0 ? hash.loc.last_line - 1 : 0;
          startCol = hash.loc.first_column > 0 ? hash.loc.first_column - 1 : 0;
          endCol = hash.loc.last_column > 0 ? hash.loc.last_column - 1 : 0;
        }
      }

      errors.push({
          message: msg,
          severity: 'error',
          from: CodeMirror.Pos(startLine, startCol),
          to: CodeMirror.Pos(endLine, endCol)
        });
    }
  }
}
