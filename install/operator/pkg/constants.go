package pkg

var DefaultOperatorImage = "docker.io/syndesis/syndesis-operator"
var DefaultOperatorTag = "latest"
var JobNameLabel = "job-name"

// The Date & Time of the build if specified, not a constant
var BuildDateTime = ""

// ControllerUidLabel label to be added to Jobs, for tracking, an actual constant
var ControllerUidLabel = "controller-uid"
