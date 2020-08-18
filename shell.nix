{
  nixpkgs ? builtins.fetchGit {
    url = https://github.com/nixos/nixpkgs-channels;
    ref = "nixpkgs-unstable";
    rev = "2d6cbbe4627f6fe4a179c681537b0a3e4f59b732";
  }
}:

let
  pkgs = import nixpkgs {config = {}; overlays = [];};
in
pkgs.mkShell {
  buildInputs = [
    pkgs.checkstyle
    pkgs.dive
    pkgs.go
    pkgs.just
    pkgs.kube3d
    pkgs.kubectl
    pkgs.kustomize
    pkgs.minikube
    pkgs.minishift
    pkgs.nodejs
    pkgs.openshift
    pkgs.operator-sdk
    pkgs.skaffold
    pkgs.yarn
  ];
  shellHook = ''
    export PATH="$PWD/node_modules/.bin/:$PATH"
    yarn install
  '';
}
