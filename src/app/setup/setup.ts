export interface Setup {
  gitHubOAuthConfiguration: GitHubOAuthConfiguration;
}

export interface GitHubOAuthConfiguration {
  clientId: string;
  clientSecret: string;
}
