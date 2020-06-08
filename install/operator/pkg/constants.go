package pkg

// DefaultOperatorImage holds the default image of the operator deployed in install, not a constant
var DefaultOperatorImage = "docker.io/syndesis/syndesis-operator"

// DefaultOperatorTag holds the default image tag of the operator image deployed in install, not a constant
var DefaultOperatorTag = "latest"

// The Date & Time of the build if specified, not a constant
var BuildDateTime = ""

// ControllerUIDLabel label to be added to Jobs, for tracking, an actual constant
const ControllerUIDLabel = "controller-uid"
