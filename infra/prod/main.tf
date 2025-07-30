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

resource "aws_vpc" "subook_vpc" {
  cidr_block           = "10.10.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "subook-vpc"
  }
}

resource "aws_subnet" "subook_subnet" {
  vpc_id                  = aws_vpc.subook_vpc.id
  cidr_block              = "10.10.1.0/24"
  map_public_ip_on_launch = true
  availability_zone       = "${var.region}a"

  tags = {
    Name = "subook-subnet"
  }
}

resource "aws_internet_gateway" "subook_igw" {
  vpc_id = aws_vpc.subook_vpc.id

  tags = {
    Name = "subook-igw"
  }
}

resource "aws_route_table" "subook_rt" {
  vpc_id = aws_vpc.subook_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.subook_igw.id
  }

  tags = {
    Name = "subook-rt"
  }
}

resource "aws_route_table_association" "subook_rt_assoc" {
  subnet_id      = aws_subnet.subook_subnet.id
  route_table_id = aws_route_table.subook_rt.id
}

resource "aws_security_group" "subook_sg" {
  name        = "subook-sg"
  description = "Allow SSH, HTTP, HTTPS"
  vpc_id      = aws_vpc.subook_vpc.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "subook-sg"
  }
}

data "aws_ami" "amazon_linux" {
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

resource "aws_instance" "subook_ec2" {
  ami                         = data.aws_ami.amazon_linux.id
  instance_type               = "t3.micro"
  subnet_id                   = aws_subnet.subook_subnet.id
  vpc_security_group_ids      = [aws_security_group.subook_sg.id]
  associate_public_ip_address = true

  tags = {
    Name = "subook-ec2"
  }

  root_block_device {
    volume_size = 25
    volume_type = "gp3"
  }

  user_data = <<-EOF
#!/bin/bash
yum install -y docker
systemctl enable docker
systemctl start docker
EOF
}

resource "aws_eip" "subook_eip" {
  instance = aws_instance.subook_ec2.id
  domain   = "vpc"

  tags = {
    Name = "subook-eip"
  }
}
