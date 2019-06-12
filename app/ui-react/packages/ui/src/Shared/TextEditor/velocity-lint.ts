import * as CodeMirror from 'codemirror';
import * as Velocity from 'velocityjs';
import { AbstractLanguageLint } from './abstract-language-lint';
import { TemplateSymbol } from './template-symbol';

import 'codemirror/mode/velocity/velocity.js';

export type LintVelocity = 'velocity';

export class VelocityLint extends AbstractLanguageLint {
  constructor() {
    super('velocity');
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
        const msg = 'linter-no-symbols';
        errors.push({
          from: CodeMirror.Pos(0, 0),
          message: msg,
          severity: 'warning',
          to: CodeMirror.Pos(0, 0),
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
        from: CodeMirror.Pos(startLine, startCol),
        message: msg,
        severity: 'error',
        to: CodeMirror.Pos(endLine, endCol),
      });
    }
  }
}
