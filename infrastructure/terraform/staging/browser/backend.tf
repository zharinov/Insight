terraform {
  backend "s3" {
    bucket = "insights-terraform"
    key    = "staging/browser"
    region = "us-east-1"
  }
}
