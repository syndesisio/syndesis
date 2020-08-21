import (builtins.fetchGit {
  url = https://github.com/nixos/nixpkgs-channels;
  ref = "nixpkgs-unstable";
  rev = "16fc531784ac226fb268cc59ad573d2746c109c1";
}) { config = {}; overlays = []; }
