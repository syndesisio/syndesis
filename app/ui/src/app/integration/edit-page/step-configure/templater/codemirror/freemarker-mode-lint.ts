import {
  Injectable
} from '@angular/core';
import {
  I18NService
} from '@syndesis/ui/platform';
import {
  CodeMirror,
  FreemarkerParser
} from '@syndesis/ui/vendor';
import { AbstractLanguageLint } from './abstract-language-lint';
import { TemplateSymbol } from './template-symbol';

@Injectable()
export class FreemarkerModeLint extends AbstractLanguageLint {

  constructor(protected i18NService: I18NService) {
    super('freemarker', i18NService);
  }

  public parse(content: string): any[] {
    const symbols: TemplateSymbol[] = [];

    const parser: FreemarkerParser = new FreemarkerParser();
    const result: any = parser.parse(content);
    for (const token of result.tokens) {
      if (token.type === 'Interpolation') {
        symbols.push(new TemplateSymbol(token['params'], 'string'));
      }
    }
    return symbols;
  }

  protected define(): void {
    CodeMirror.defineMode(
      this.name(),
      (config, parserConfig) => {
        return {
          token: (stream, state) => {
            let ch;
            if (stream.match('${')) {
              // tslint:disable-next-line
              while ((ch = stream.next()) != null) {
                if (ch == '}') {
                  return this.name();
                }
              }
            }

            while (stream.next() != null && !stream.match('${', false)) {
              // Read it but don't do anything
            }

            return null;
          }
        };
      }
    );

    super.define();
  }

  protected validate(text: string, errors: any[]): void {
    try {
      const parser: FreemarkerParser = new FreemarkerParser();
      const result: any = parser.parse(text);

      if (result.ast && result.ast.errors) {
        for (const error of result.ast.errors) {
          const startLine = error.loc.start.line > 0 ? error.loc.start.line - 1 : 0;
          const startCol = error.loc.start.column > 0 ? error.loc.start.column - 1 : 0;
          const endLine = error.loc.end.line > 0 ? error.loc.end.line - 1 : 0;
          const endCol = error.loc.end.column > 0 ? error.loc.end.column - 1 : 0;

          errors.push({
            message: error.message,
            severity: 'error',
            from: CodeMirror.Pos(startLine, startCol),
            to: CodeMirror.Pos(endLine, endCol)
          });
        }
      }

      let totalSymbols = 0;
      for (const token of result.tokens) {
        if (token.type === 'Interpolation') {
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
      errors.push({
          message: exception.message,
          severity: 'error',
          from: CodeMirror.Pos(0, 0),
          to: CodeMirror.Pos(0, 0)
        });
    }
  }
}
