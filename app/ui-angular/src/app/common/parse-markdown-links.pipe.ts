import { DomSanitizer } from '@angular/platform-browser';
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'synParseMarkdownLinks'
})
export class ParseMarkdownLinksPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(value: string): any {
    const parsedText = value.replace(
      /\[(.+?)\]\((https?:\/\/[a-zA-Z0-9/.(]+?)\)/g,
      '<a href="$2">$1</a>'
    );
    return this.sanitizer.bypassSecurityTrustHtml(parsedText);
  }
}
