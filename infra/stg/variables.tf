variable "prefix" {
  description = "Prefix for all resources"
  default     = "subook"
}

variable "region" {
  description = "AWS region"
  default     = "ap-northeast-2"
}

variable "nickname" {
  description = "nickname"
  default     = "subook"
}

variable "ami_id" {
  type        = string
  description = "Pinned AMI ID for stg EC2 (prevents unintended instance replacement)"
  default     = "ami-0811349cae530179a"
}