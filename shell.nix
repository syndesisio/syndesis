{
  pkgs ? import ./nix/nixpkgs.nix
}:

let
  syndesis-operator = pkgs.callPackage ./nix/syndesis-operator.nix { inherit (pkgs.stdenv) mkDerivation; };
in
pkgs.mkShell {
  buildInputs = [
    syndesis-operator
    pkgs.checkstyle
    pkgs.dive
    pkgs.docker
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
    pkgs.podman
    pkgs.skaffold
    pkgs.yarn
  ];
  shellHook = ''
    export PATH="$PWD/node_modules/.bin/:$PATH"
    export OPERATOR_BINARY=${syndesis-operator}/bin/syndesis-operator
    yarn install
  '';
}
