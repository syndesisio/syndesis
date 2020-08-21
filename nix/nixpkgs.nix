import (builtins.fetchTarball {
  url = https://github.com/nixos/nixpkgs-channels/tarball/16fc531784ac226fb268cc59ad573d2746c109c1;
  # hash obtained via `nix-prefetch-url --unpack https://github.com/nixos/nixpkgs-channels/tarball/{commit}`
  sha256 = "0qw1jpdfih9y0dycslapzfp8bl4z7vfg9c7qz176wghwybm4sx0a";
}) { config = {}; overlays = []; }
