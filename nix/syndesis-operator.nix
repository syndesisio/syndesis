{ autoPatchelfHook, cacert, curl, mkDerivation, ... }:

mkDerivation rec {
  pname = "syndesis-operator";
  version = "2.0.0-20200727";
  src = mkDerivation {
    name = "sysndesis-operator-bin";
    phases = [ "buildPhase" ];
    buildInputs = [ curl cacert ];
    buildPhase = ''
      export SSL_CERT_FILE=${cacert}/etc/ssl/certs/ca-bundle.crt
      curl -L https://github.com/syndesisio/syndesis/releases/download/$version/syndesis-operator-linux-amd64.tar.gz > $out
    '';
    outputHash = "sha256:1f6v8gq0cgq5q7szcj0frf9lv6azk6m5lqabrmg83pzm6p2lf735";
  };
  buildInputs = [ autoPatchelfHook ];
  phases = [ "unpackPhase" "fixupPhase" ];
  unpackPhase = ''
    tar xf $src
    mkdir -p $out/bin/
    cp syndesis-operator $out/bin/
  '';
}