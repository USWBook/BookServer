terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
  }
}

provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project   = "subook"
      ManagedBy = "Terraform"
    }
  }
}

resource "aws_vpc" "subook_vpc_stg" {
  cidr_block           = "10.9.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "subook-vpc-stg"
  }
}

resource "aws_subnet" "subook_subnet_stg" {
  vpc_id                  = aws_vpc.subook_vpc_stg.id
  cidr_block              = "10.9.1.0/24"
  availability_zone       = "${var.region}a"
  map_public_ip_on_launch = true

  tags = {
    Name = "subook-subnet-stg"
  }
}

resource "aws_internet_gateway" "subook_igw_stg" {
  vpc_id = aws_vpc.subook_vpc_stg.id

  tags = {
    Name = "subook-igw-stg"
  }
}

resource "aws_route_table" "subook_rt_stg" {
  vpc_id = aws_vpc.subook_vpc_stg.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.subook_igw_stg.id
  }

  tags = {
    Name = "subook-rt-stg"
  }
}

resource "aws_route_table_association" "subook_association_stg" {
  subnet_id      = aws_subnet.subook_subnet_stg.id
  route_table_id = aws_route_table.subook_rt_stg.id
}

resource "aws_security_group" "subook_sg_stg" {
  name   = "subook-sg-stg"
  vpc_id = aws_vpc.subook_vpc_stg.id

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "subook-sg-stg"
  }
}

resource "aws_instance" "subook_ec2_stg" {
  ami                         = data.aws_ami.amazon_linux_stg.id
  instance_type               = "t3.micro"
  subnet_id                   = aws_subnet.subook_subnet_stg.id
  vpc_security_group_ids      = [aws_security_group.subook_sg_stg.id]
  associate_public_ip_address = true

  tags = {
    Name = "subook-ec2-stg"
  }

  root_block_device {
    volume_type = "gp3"
    volume_size = 25
  }

  user_data = <<-EOF
#!/bin/bash
echo "Hello from STG EC2" > /home/ec2-user/hello.txt
EOF
}

resource "aws_iam_role" "subook_ec2_role_stg" {
  name = "subook-ec2-role-stg"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "",
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "s3_full_access_stg" {
  role       = aws_iam_role.subook_ec2_role_stg.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

resource "aws_iam_role_policy_attachment" "ec2_ssm_stg" {
  role       = aws_iam_role.subook_ec2_role_stg.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2RoleforSSM"
}

resource "aws_iam_instance_profile" "subook_instance_profile_stg" {
  name = "subook-instance-profile-stg"
  role = aws_iam_role.subook_ec2_role_stg.name
}

resource "aws_eip" "subook_eip_stg" {
  domain   = "vpc"
  instance = aws_instance.subook_ec2_stg.id
  tags = {
    Name = "subook-eip-stg"
  }
}

data "aws_ami" "amazon_linux_stg" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }

  filter {
    name   = "architecture"
    values = ["x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }
}
