export class TemplateSymbol {
  private id: string;

  private type: string;

  constructor(id: string, type: string) {
    this.id = id;
    this.type = type;
  }

  public getId(): string {
    return this.id;
  }

  public getType(): string {
    return this.type;
  }
}
