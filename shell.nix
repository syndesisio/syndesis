let
  pkgs = import <nixpkgs> {};
in
pkgs.mkShell {
  buildInputs = [
    pkgs.checkstyle
    pkgs.dive
    pkgs.just
    pkgs.kube3d
    # pkgs.kubebox
    pkgs.kubectl
    # pkgs.kubespy
    pkgs.minikube
    pkgs.minishift
    pkgs.nodejs
    pkgs.yarn
    pkgs.openshift
    pkgs.skaffold
  ];
  shellHook = ''
    export PATH="$PWD/node_modules/.bin/:$PATH"
    yarn install
  '';
}
