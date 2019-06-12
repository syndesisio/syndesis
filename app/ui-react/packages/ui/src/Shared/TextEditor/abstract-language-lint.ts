import * as CodeMirror from 'codemirror';
import { TemplateSymbol } from './template-symbol';

export abstract class AbstractLanguageLint {
  private errors: any[] = [];

  constructor(private theName: string) {
    this.define();
  }

  public abstract parse(content: string): TemplateSymbol[];

  public name(): string {
    return this.theName;
  }

  public validator(text: string, options: any): any[] {
    this.errors = [];
    if (text.length === 0) {
      const msg = 'linter-no-content';
      this.errors.push({
        from: CodeMirror.Pos(0, 0),
        message: msg,
        severity: 'warning',
        to: CodeMirror.Pos(0, 0),
      });
      return this.errors;
    }
    this.validate(text, this.errors);
    return this.errors;
  }

  protected define(): void {
    CodeMirror.registerHelper(
      'lint',
      this.name(),
      (text: string, options: {}) => this.validator(text, options)
    );
  }

  protected abstract validate(text: string, errors: any[]): void;
}
